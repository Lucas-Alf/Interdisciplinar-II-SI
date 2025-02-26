package br.com.setrem.interdisciplinarII.beans;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.setrem.interdisciplinarII.model.CliFor;
import br.com.setrem.interdisciplinarII.model.MotivoBaixa;
import br.com.setrem.interdisciplinarII.repository.MotivoBaixaRepository;

@Named(value = "motivoBaixaBean")
@SessionScoped
public class MotivoBaixaBean implements Serializable {

    @Autowired
    private MotivoBaixaRepository motivoBaixaRepository;
    private MotivoBaixa motivoBaixa = new MotivoBaixa();

    private int id;
    private String descricao;
    private List<MotivoBaixa> motivoBaixas;

    public MotivoBaixaBean() {

    }

    public void AtualizarTabela() {
        CliFor empresa = (CliFor) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("empresa");
        this.motivoBaixas = motivoBaixaRepository.AtualizarTabela(empresa.getId());
    }

    public void Pesquisar(String descricao) {
        CliFor empresa = (CliFor) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("empresa");
        this.motivoBaixas = motivoBaixaRepository.Pesquisar(descricao, empresa.getId());
    }

    public void AbrirModal() {
        this.motivoBaixa = new MotivoBaixa();
        PrimeFaces.current().executeScript("$('#CadastrarMotivoBaixa').modal('show');");
    }

    public void Salvar() {
        if (this.motivoBaixa.getDescricao().equals("")) {
            FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_INFO, "Atenção!", "Informe uma Descrição!");
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage("validacao", fm);
        } else {
            CliFor empresa = (CliFor) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("empresa");
            motivoBaixa.setCliForid(empresa);
            motivoBaixaRepository.save(this.motivoBaixa);
            this.AtualizarTabela();

            FacesContext.getCurrentInstance().getPartialViewContext().setRenderAll(true);
            PrimeFaces.current().executeScript("$('#CadastrarMotivoBaixa').modal('hide');");

            FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Salvo com sucesso.");
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage("validacao2", fm);
        }
    }

    public void Deletar(int id) {
        try {
            if (id == 0) {
                FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção!","Selecione um registro para Excluir.");
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage("validacao2", fm);
            } else {
                motivoBaixaRepository.deleteById(id);
                this.AtualizarTabela();

                FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, "", "Registro deletado.");
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage("validacao2", fm);
            }
        } catch (Exception e) {
            FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_FATAL, "Atenção!","Não é possível excluir, pois possui relação com outro registro.");
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage("validacao2", fm);
        }
    }

    public void AbreAlterar(int id) {
        if (id == 0) {
            FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção!","Selecione um registro para Alterar.");
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage("validacao2", fm);
        } else {
            motivoBaixa = motivoBaixaRepository.getOne(id);
            PrimeFaces.current().executeScript("$('#CadastrarMotivoBaixa').modal('show');");
        }
    }

    public void Alterar() {
        motivoBaixaRepository.save(motivoBaixa);
        this.AtualizarTabela();
        PrimeFaces.current().executeScript("$('.modal-backdrop').hide();");

        FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Registro alterado.");
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, fm);
    }

    public MotivoBaixa getMotivoBaixa() {
        return motivoBaixa;
    }

    public void setMotivoBaixa(MotivoBaixa motivoBaixa) {
        this.motivoBaixa = motivoBaixa;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<MotivoBaixa> getMotivoBaixas() {
        if (this.motivoBaixas == null) {
            this.motivoBaixas = motivoBaixaRepository.findAll();
        }
        return motivoBaixas;
    }

    public void setMotivoBaixas(List<MotivoBaixa> motivoBaixas) {
        this.motivoBaixas = motivoBaixas;
    }

}