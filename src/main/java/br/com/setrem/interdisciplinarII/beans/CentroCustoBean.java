package br.com.setrem.interdisciplinarII.beans;

import br.com.setrem.interdisciplinarII.SessionFactory;
import br.com.setrem.interdisciplinarII.model.CentroCusto;
import br.com.setrem.interdisciplinarII.model.Usuario;
import br.com.setrem.interdisciplinarII.repository.CentroCustoRepository;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;

@Named(value = "centroCustoBean")
@SessionScoped
public class CentroCustoBean implements Serializable {

    @Autowired
    private CentroCustoRepository centroCustoRepository;

    public CentroCustoBean() {
    }

    public void Insert(String nome) {
        CentroCusto centCust = new CentroCusto();
        centCust.setNome(nome);
        //centCust.setCliForid();
        centroCustoRepository.save(centCust);
    }

    public List<CentroCusto> AtualizarTable() {
        return centroCustoRepository.findAll();
    }

    public void Remove(int id) {
        if (id == 0) {
            FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_INFO, "Atenção!", "Selecione um registro para excluir.");
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, fm);

        } else {
            centroCustoRepository.deleteById(id);
        }

    }

}
