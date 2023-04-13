package curso.api.rest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import curso.api.rest.model.Profissao;
import curso.api.rest.repositoy.ProfissaoRepository;

@RestController
@RequestMapping(value = "/profissao")
public class ProfissaoController {
	
	@Autowired
	private ProfissaoRepository profissaoRepository;
	
	/*Lista as profissões que podem ser associadas ao usuário*/
	@GetMapping(value = "/", produces="application/json")
	public ResponseEntity<List<Profissao>> profissoes() {
		
		List<Profissao> lista = profissaoRepository.findAll();
		
		return new ResponseEntity<List<Profissao>>(lista, HttpStatus.OK);
	}
}
