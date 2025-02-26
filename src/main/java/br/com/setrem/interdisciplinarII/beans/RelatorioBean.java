/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.setrem.interdisciplinarII.beans;

import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.imageio.stream.ImageInputStream;
import javax.inject.Named;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.swing.ImageIcon;

import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import br.com.setrem.interdisciplinarII.model.CliFor;
import br.com.setrem.interdisciplinarII.model.FiltroRelatorio;
import br.com.setrem.interdisciplinarII.model.Relatorio;
import br.com.setrem.interdisciplinarII.repository.FiltroRelatorioRepository;
import br.com.setrem.interdisciplinarII.repository.RelatorioRepository;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRResultSetDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;

@Named(value = "relatorioBean")
@SessionScoped
public class RelatorioBean implements Serializable {

    @Autowired
    private RelatorioRepository relatorioRepository;
    @Autowired
    private FiltroRelatorioRepository filtroRelatorioRepository;
    @Autowired
    private ResourceLoader resourceLoader;

    private String descricaoBusca = "";
    private List<Relatorio> listaRelatorio = new ArrayList<Relatorio>();
    private List<FiltroRelatorio> listaFiltroRelatorio = new ArrayList<FiltroRelatorio>();
    private List<FiltroRelatorio> listaFiltroRelatorioSelecionados = new ArrayList<FiltroRelatorio>();

    public String getDescricaoBusca() {
        return descricaoBusca;
    }

    public void setDescricaoBusca(String descricaoBusca) {
        this.descricaoBusca = descricaoBusca;
    }

    public List<Relatorio> getListaRelatorio() {
        return listaRelatorio;
    }

    public void setListaRelatorio(List<Relatorio> listaRelatorio) {
        this.listaRelatorio = listaRelatorio;
    }

    public RelatorioBean() {
    }

    public void lista() {
        CliFor empresa = (CliFor) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("empresa");
        this.listaRelatorio = relatorioRepository.Lista(this.descricaoBusca, empresa.getId());
    }

    public List<FiltroRelatorio> getListaFiltroRelatorio() {
        return listaFiltroRelatorio;
    }

    public void setListaFiltroRelatorio(List<FiltroRelatorio> listaFiltroRelatorio) {
        this.listaFiltroRelatorio = listaFiltroRelatorio;
    }

    public List<FiltroRelatorio> getListaFiltroRelatorioSelecionados() {
        return listaFiltroRelatorioSelecionados;
    }

    public void setListaFiltroRelatorioSelecionados(List<FiltroRelatorio> listaFiltroRelatorioSelecionados) {
        this.listaFiltroRelatorioSelecionados = listaFiltroRelatorioSelecionados;
    }

    public void abreImpressao(int relatorioId) {
        if (relatorioId == 0) {
            FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção!",
                    "Selecione um relatório para imprimir.");
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, fm);
        } else {
            this.listaFiltroRelatorio = filtroRelatorioRepository.listar(relatorioId);
            if (listaFiltroRelatorio.size() == 0) {
                this.listaFiltroRelatorioSelecionados = new ArrayList<FiltroRelatorio>();
            }
            PrimeFaces.current().executeScript("$('#impressaoModal').modal('show');");
        }
    }

    private Connection abreConexao() {
        try {
            String url = "jdbc:postgresql://ec2-50-19-95-77.compute-1.amazonaws.com:5432/d43v5g6nfkagp";
            String usuario = "pnbqtudcpohwgn";
            String senha = "c3265949766568e312fdc45e956de4aa8eec60121034cf0871821674bce06027";
            return DriverManager.getConnection(url, usuario, senha);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    private void fechaConexao(Connection conexao) {
        try {
            conexao.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private String gerarConsulta(Relatorio relatorio, List<FiltroRelatorio> filtrosList) {
        String sql = relatorio.getSqlquery();
        CliFor empresa = (CliFor) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("empresa");
        if (sql.contains(":empresa")) {
            sql = sql.replace(":empresa", "'" + empresa.getId().replaceAll("\\W+", "") + "'");
        }
        FiltroRelatorio[] filtros = filtrosList.toArray(new FiltroRelatorio[filtrosList.size()]);
        String sqlWhere = "";
        if (filtros.length > 0) {
            sqlWhere += " WHERE ";
            for (int i = 0; i < filtros.length; i++) {
                if (i == 0) {
                    sqlWhere = sqlWhere + " " + filtros[i].getSqlwhere();
                } else {
                    sqlWhere = sqlWhere + " " + ("AND " + filtros[i].getSqlwhere());
                }
            }
            sql = "select * from (" + sql + " ) as consulta" + relatorio.getId() + relatorio.getCliForid() + sqlWhere;
        }
        return sql;
    }

    private JRResultSetDataSource gerarDataSource(String consulta) {
        Connection conexao = abreConexao();
        try {
            PreparedStatement statement = conexao.prepareStatement(consulta);
            ResultSet result = statement.executeQuery();
            return new JRResultSetDataSource(result);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        } finally {
            fechaConexao(conexao);
        }
    }

    private JRBeanArrayDataSource gerarDataSourceHibernate(String consulta) {
        try {
            List<?> result = relatorioRepository.RealizaConsultaBalanceteVerificacao();
            return new JRBeanArrayDataSource(result.toArray());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    private JasperPrint exportPdfFile(Relatorio relatorio) {
        try {
            Resource resource = resourceLoader
                    .getResource("classpath:/META-INF/resources/reports/" + relatorio.getNome() + ".jrxml");
            Resource logoResource = resourceLoader.getResource("classpath:/META-INF/resources/reports/abaco.jpg");
            InputStream is = logoResource.getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
              buffer.write(data, 0, nRead);
            }
            Image logo = new ImageIcon(buffer.toByteArray()).getImage();
            JasperReport jasperReport = JasperCompileManager.compileReport(resource.getInputStream());
            CliFor empresa = (CliFor) FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
                    .get("empresa");
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("LOGO_ABACO", logo);
            parameters.put("EMPRESA_NOME", empresa.getNome());
            parameters.put("EMPRESA_CNPJ", empresa.getCnpj());
            parameters.put("EMPRESA_ID", empresa.getId());
            parameters.put("EMPRESA_CELULAR", empresa.getCelular());
            parameters.put("EMPRESA_EMAIL", empresa.getEmail());
            parameters.put("RELATORIO_NOME", relatorio.getNome());
            JRResultSetDataSource resultSet = gerarDataSource(
                    gerarConsulta(relatorio, this.listaFiltroRelatorioSelecionados));
            // JRBeanArrayDataSource resultSet = gerarDataSourceHibernate(
            // gerarConsulta(relatorio, this.listaFiltroRelatorioSelecionados));
            java.util.Locale locale = new Locale( "pt", "BR" );
            parameters.put( JRParameter.REPORT_LOCALE, locale );
            JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, resultSet);
            return print;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public void imprimirRelatorio(int relatorioId) {
        Optional<Relatorio> relatorio = relatorioRepository.findById(relatorioId);
        if (relatorio.isPresent()) {
            try {
                JasperPrint jasperPrint = exportPdfFile(relatorio.get());
                if (jasperPrint == null) {
                    throw new Exception("Erro ao exportar o relatório! (jasperPrint is null)");
                }
                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
                ServletOutputStream outputStream = response.getOutputStream();
                JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
                response.setContentType("application/pdf");
                facesContext.responseComplete();
                outputStream.flush();
                outputStream.close();
                facesContext.renderResponse();
                response.setHeader("Content-Disposition", "attachment; filename=" + relatorio.get().getNome() + ".pdf");
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        } else {
            System.err.println("Relatório não encontrado!");
        }
    }
}