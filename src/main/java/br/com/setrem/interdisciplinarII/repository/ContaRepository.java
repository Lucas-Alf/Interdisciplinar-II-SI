package br.com.setrem.interdisciplinarII.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import br.com.setrem.interdisciplinarII.model.Conta;

@Repository
public interface ContaRepository extends JpaRepository<Conta, Integer> {

    @Query("SELECT c FROM conta c WHERE c.descricao LIKE %?1%")
    public List<Conta> pesquisar(String nome);
}