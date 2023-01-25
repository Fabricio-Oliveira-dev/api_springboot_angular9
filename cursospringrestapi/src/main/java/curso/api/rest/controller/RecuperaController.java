package curso.api.rest.controller;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import curso.api.rest.ObjetoError;
import curso.api.rest.model.Usuario;
import curso.api.rest.repositoy.UsuarioRepository;
import curso.api.rest.service.ServiceEnviaEmail;

@RestController
@RequestMapping(value = "/recuperar")
public class RecuperaController {
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private ServiceEnviaEmail serviceEnviaEmail;
	
	@ResponseBody
	@PostMapping(value = "/")
	public ResponseEntity<ObjetoError> recuperar (@RequestBody Usuario login) throws Exception {
		
		ObjetoError objetoError = new ObjetoError();
		
		Usuario user = usuarioRepository.findUserByLogin(login.getLogin());
		
		if (user == null) {
			objetoError.setCode("404"); /*Não encontrado*/
			objetoError.setError("Usuário não encontrado");
		} else {
			/*Rotina de envio de e-mail*/
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String senhaNova = dateFormat.format(Calendar.getInstance().getTime());
			String senhaCriptografada = new BCryptPasswordEncoder().encode(senhaNova);
			
			usuarioRepository.updateSenha(senhaCriptografada, user.getId());
			
			serviceEnviaEmail.enviarEmail("Recuperação de senha", user.getLogin(), "Sua nova senha é: " + senhaNova);
			
			objetoError.setCode("200"); /*Encontrado*/
			objetoError.setError("Acesso enviado para o seu e-mail");
		}
		return new ResponseEntity<ObjetoError>(objetoError, HttpStatus.OK);
	}
}
