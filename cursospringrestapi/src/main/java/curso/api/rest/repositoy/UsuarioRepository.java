package curso.api.rest.repositoy;

import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import curso.api.rest.model.Usuario;

@Repository
public interface UsuarioRepository  extends JpaRepository<Usuario, Long>{
	
	/*Query para achar o usuário com base no login*/
	@Query("SELECT u FROM Usuario u WHERE u.login = ?1")
	Usuario findUserByLogin(String login);
	
	/*Query que atualiza o token*/
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value = "UPDATE usuario SET token = ?1 where login = ?2")
	void atualizaTokenUser(String token, String login);
	
	/*Query para listar o usuário com base no nome*/
	@Query("SELECT u FROM Usuario u WHERE u.nome LIKE %?1%")
	List<Usuario> findUserByNome(String nome);
	
	/*Query que consulta o nível hierárquico do usuário. e.g. ADMIN, USER*/
	@Query(value="SELECT constraint_name FROM information_schema.constraint_column_usage  WHERE table_name = 'usuarios_role' AND column_name = 'role_id' AND constraint_name <> 'unique_role_user';", nativeQuery = true)
	String consultaConstraintRole();
	
	/*Query adiciona o nível hierárquico padrão para o usuário*/
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value = "INSERT INTO usuarios_role (usuario_id, role_id) values(?1, (SELECT id FROM role WHERE nome_role = 'ROLE_USER')); ")
	void insereAcessoRolePadrao(Long idUser);
	
	/*Query que atualiza a senha*/
	@Transactional
	@Modifying
	@Query(value = "UPDATE usuario SET senha = ?1 WHERE id = ?2", nativeQuery = true)
	void updateSenha(String senha, Long codUser);
	
	default Page<Usuario> findUserByNamePage(String nome, PageRequest pageRequest) {
		
		Usuario usuario = new Usuario();
		usuario.setNome(nome);
		
		/*Configurando para pesquisar por nome e por paginação*/
		ExampleMatcher exampleMatcher = ExampleMatcher.matchingAny()
				.withMatcher("nome", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
		
		Example<Usuario> example = Example.of(usuario, exampleMatcher);
		
		Page<Usuario> retorno = findAll(example, pageRequest);
		
		return retorno;
	}
}
