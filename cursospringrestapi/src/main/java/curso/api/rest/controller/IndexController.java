package curso.api.rest.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import curso.api.rest.model.UserChart;
import curso.api.rest.model.UserReport;
import curso.api.rest.model.Usuario;
import curso.api.rest.model.UsuarioDTO;
import curso.api.rest.repositoy.TelefoneRepository;
import curso.api.rest.repositoy.UsuarioRepository;
import curso.api.rest.service.ImplementacaoUserDetailsService;
import curso.api.rest.service.ServiceRelatorio;

@RestController
@RequestMapping(value = "/usuario")
public class IndexController {
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private TelefoneRepository telefoneRepository;
	
	@Autowired
	private ImplementacaoUserDetailsService implementacaoUserDetailsService;
	
	@Autowired
	private ServiceRelatorio serviceRelatorio;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/* Prepara o relatório com base no id */
	@GetMapping(value = "/{id}/codigovenda/{venda}", produces = "application/json")
	public ResponseEntity<Usuario> relatorio(@PathVariable (value = "id") Long id
			                                						, @PathVariable (value = "venda") Long venda) {
		
		Optional<Usuario> usuario = usuarioRepository.findById(id);
		
		/*retorna o relatório*/
		return new ResponseEntity<Usuario>(usuario.get(), HttpStatus.OK);
	}
	
	/* versão 2 do END-POINT para listar os usuários ao entrar no sistema */
	@GetMapping(value = "/{id}", produces = "application/json")
	@CachePut("cacheuser")
	public ResponseEntity<Usuario> initV2(@PathVariable (value = "id") Long id) {
		
		Optional<Usuario> usuario = usuarioRepository.findById(id);
		
		return new ResponseEntity<Usuario>(usuario.get(), HttpStatus.OK);
	}
	
	@DeleteMapping(value = "/{id}", produces = "application/text")
	public String delete (@PathVariable("id") Long id){
		
		usuarioRepository.deleteById(id);
		
		return "ok";
	}
	
	@DeleteMapping(value = "/{id}/venda", produces = "application/text")
	public String deletevenda(@PathVariable("id") Long id){
		
		usuarioRepository.deleteById(id);
		
		return "ok";
	}
	
	@GetMapping(value = "/", produces = "application/json")
	@CacheEvict(value="cacheusuarios", allEntries = true)
	@CachePut("cacheusuarios")
	public ResponseEntity<Page<Usuario>> usuario () throws InterruptedException{
		
		PageRequest page = PageRequest.of(0, 5, Sort.by("nome"));
		Page<Usuario> list = usuarioRepository.findAll(page);
		
		return new ResponseEntity<Page<Usuario>>(list, HttpStatus.OK);
	}
	
	@GetMapping(value = "/page/{pagina}", produces = "application/json")
	@CacheEvict(value="cacheusuarios", allEntries = true)
	@CachePut("cacheusuarios")
	public ResponseEntity<Page<Usuario>> usuarioPagina (@PathVariable("pagina") int pagina) throws InterruptedException{
		
		PageRequest page = PageRequest.of(pagina, 5, Sort.by("nome"));
		Page<Usuario> list = usuarioRepository.findAll(page);
		
		return new ResponseEntity<Page<Usuario>>(list, HttpStatus.OK);
	}
	
	/*END-POINT de consulta de usuário por nome*/
	@GetMapping(value = "/usuarioPorNome/{nome}", produces = "application/json")
	@CacheEvict(value="cacheusuarios", allEntries = true)
	@CachePut("cacheusuarios")
	public ResponseEntity<Page<Usuario>> usuarioPorNome (@PathVariable("nome") String nome) throws InterruptedException{
		
		PageRequest pageRequest = null;
		Page<Usuario> list = null;
		
		if (nome == null || (nome != null && nome.trim().isEmpty()) || nome.equalsIgnoreCase("undefined")) { /*Não informou nome*/
			
			pageRequest = PageRequest.of(0, 5, Sort.by("nome"));
			list = usuarioRepository.findAll(pageRequest);
		} 
		else {
			pageRequest = PageRequest.of(0, 5, Sort.by("nome"));
			list = usuarioRepository.findUserByNamePage(nome, pageRequest);
		}
		return new ResponseEntity<Page<Usuario>>(list, HttpStatus.OK);
	}
	
	/*END-POINT de consulta de usuário por nome*/
	@GetMapping(value = "/usuarioPorNome/{nome}/page/{page}", produces = "application/json")
	@CacheEvict(value="cacheusuarios", allEntries = true)
	@CachePut("cacheusuarios")
	public ResponseEntity<Page<Usuario>> usuarioPorNomePage (@PathVariable("nome") String nome, 
																									@PathVariable("page") int page) throws InterruptedException{
		
		PageRequest pageRequest = null;
		Page<Usuario> list = null;
		
		if (nome == null || (nome != null && nome.trim().isEmpty()) || nome.equalsIgnoreCase("undefined")) { /*Não informou nome*/
			
			pageRequest = PageRequest.of(page, 5, Sort.by("nome"));
			list = usuarioRepository.findAll(pageRequest);
		} 
		else {
			pageRequest = PageRequest.of(page, 5, Sort.by("nome"));
			list = usuarioRepository.findUserByNamePage(nome, pageRequest);
		}
		return new ResponseEntity<Page<Usuario>>(list, HttpStatus.OK);
	}
	
	/*END-POINT para cadastrar um novo usuário*/
	@PostMapping(value = "/", produces = "application/json")
	public ResponseEntity<Usuario> cadastrar(@RequestBody Usuario usuario) throws Exception {
		
		for (int pos = 0; pos < usuario.getTelefones().size(); pos ++) {
			usuario.getTelefones().get(pos).setUsuario(usuario);
		}
		
		/*Consumindo uma API de CEP*/
		if (Objects.nonNull(usuario.getCep())) {
			URL url = new URL("https://viacep.com.br/ws/"+usuario.getCep()+"/json/");
			URLConnection connection = url.openConnection();
			InputStream is = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			
			String cep = "";
			StringBuilder jsonCep = new StringBuilder();
			
			while ((cep = br.readLine()) != null) {
				jsonCep.append(cep);
			}
			
			Usuario userAux = new Gson().fromJson(jsonCep.toString(), Usuario.class);
			
			usuario.setSenha(userAux.getCep());
			usuario.setLogradouro(userAux.getLogradouro());
			usuario.setComplemento(userAux.getComplemento());
			usuario.setBairro(userAux.getBairro());
			usuario.setLocalidade(userAux.getLocalidade());
			usuario.setUf(userAux.getUf());
		}
		
		/*Criptografando a senha*/
		String senhacriptografa = new BCryptPasswordEncoder().encode(usuario.getSenha());
		usuario.setSenha(senhacriptografa);
		Usuario usuarioSalvo = usuarioRepository.save(usuario);
		
		/*Insere o nível hierarquico do usuário. e.g. ADMIN, USER*/
		implementacaoUserDetailsService.insereAcessoPadrao(usuarioSalvo.getId());
		
		return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.OK);
	}
	
	/*Atualiza o cadastro*/
	@PutMapping(value = "/", produces = "application/json")
	public ResponseEntity<Usuario> atualizar(@RequestBody Usuario usuario) {
		
		for (int pos = 0; pos < usuario.getTelefones().size(); pos ++) {
			usuario.getTelefones().get(pos).setUsuario(usuario);
		}
		
		Usuario userTemporario = usuarioRepository.findById(usuario.getId()).get();
		
		if (!userTemporario.getSenha().equals(usuario.getSenha())) { /*senhas diferentes*/
			
			String senhacriptografa = new BCryptPasswordEncoder().encode(usuario.getSenha());
			usuario.setSenha(senhacriptografa);
		}
		
		Usuario usuarioSalvo = usuarioRepository.save(usuario);
		
		return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.OK);
	}
	
	@PutMapping(value = "/{iduser}/idvenda/{idvenda}", produces = "application/json")
	public ResponseEntity updateVenda(@PathVariable Long iduser, 
			                                     			@PathVariable Long idvenda) {

		return new ResponseEntity("Venda atualzada", HttpStatus.OK);
	}
	
	@PostMapping(value = "/{iduser}/idvenda/{idvenda}", produces = "application/json")
	public ResponseEntity cadastrarvenda(@PathVariable Long iduser, 
			                                     				@PathVariable Long idvenda) {
		
		return new ResponseEntity("id user :" + iduser + " idvenda :"+ idvenda, HttpStatus.OK);
	}
	
	/*deleta um telefone associado ao usuário*/
	@DeleteMapping(value = "/removerTelefone/{id}", produces = "application/text")
	public String deleteTelefone(@PathVariable("id") Long id) {
		
		telefoneRepository.deleteById(id);
		
		return "ok";
	}
	
	/*END-POINT para download do relatório*/
	@GetMapping(value = "/relatorio", produces = "application/text")
	public ResponseEntity<String> downloadRelatorio(HttpServletRequest request) throws Exception {
		
		byte[] pdf = serviceRelatorio.gerarRelatorio("relatorio-usuario", new HashMap(), request.getServletContext());
		
		String base64Pdf = "data:application/pdf;base64," + Base64.encodeBase64String(pdf);
		
		return new ResponseEntity<String>(base64Pdf, HttpStatus.OK);
	}
	
	/*END-POINT para download do relatório com base nos parâmetros de data*/
	@PostMapping(value = "/relatorio/", produces = "application/text")
	public ResponseEntity<String> downloadRelatorioParam(HttpServletRequest request,
			@RequestBody UserReport userReport) throws Exception {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat dateFormatParam = new SimpleDateFormat("yyyy-MM-dd");
		
		String dataInicio = dateFormatParam.format(dateFormat.parse(userReport.getDataInicio()));
		String dataFim = dateFormatParam.format(dateFormat.parse(userReport.getDataFim()));
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("DATA_INICIO", dataInicio);
		params.put("DATA_FIM", dataFim);
		
		byte[] pdf = serviceRelatorio.gerarRelatorio("relatorio-usuario-param", params, request.getServletContext());
		
		String base64Pdf = "data:application/pdf;base64," + Base64.encodeBase64String(pdf);
		
		return new ResponseEntity<String>(base64Pdf, HttpStatus.OK);
	}
	
	/*END-POINT para a criação do gráfico*/
	@GetMapping(value = "/grafico", produces = "application/json")
	public ResponseEntity<UserChart> grafico() {
		
		UserChart userChart = new UserChart();
		
		List<String> resultado = jdbcTemplate.queryForList(
				"SELECT array_agg(nome) FROM usuario WHERE salario > 0 AND nome <> ''"
				+ " UNION ALL "
				+ " SELECT CAST(array_agg(salario) AS character varying[]) FROM usuario WHERE salario > 0 AND nome <> ''", String.class);
		
		if (!resultado.isEmpty()) {
			String nomes = resultado.get(0).replaceAll("\\{", "").replaceAll("\\}", "");
			String salario = resultado.get(1).replaceAll("\\{", "").replaceAll("\\}", "");
			
			userChart.setNome(nomes);
			userChart.setSalario(salario);
		}
		return new ResponseEntity<UserChart>(userChart, HttpStatus.OK);
	}
}
