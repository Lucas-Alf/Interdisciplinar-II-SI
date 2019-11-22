package br.com.setrem.interdisciplinarII.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.com.setrem.interdisciplinarII.model.GrupoBem;

@Repository
public interface GrupoBemRepository extends JpaRepository<GrupoBem, Integer> {

    @Query(value = "select * from grupobem where descricao like %?1% and cliforid = ?2", nativeQuery = true)
    public List<GrupoBem> Pesquisar(String descricao, String empresa);

    @Query(value = "select * from grupobem where cliforid = ?1", nativeQuery = true)
    public List<GrupoBem> AtualizarTabela(String empresa);
    
}