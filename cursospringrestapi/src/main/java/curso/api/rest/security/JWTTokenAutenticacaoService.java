package curso.api.rest.security;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import curso.api.rest.ApplicationContextLoad;
import curso.api.rest.model.Usuario;
import curso.api.rest.repositoy.UsuarioRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
@Component
public class JWTTokenAutenticacaoService {
	
	/*Tempo de validade do Token 2 dias*/
	private static final long EXPIRATION_TIME = 172800000;
	
	/*Uma senha única para compor a autenticacao e ajudar na segurança*/
	private static final String SECRET = "SenhaExtremamenteSecreta";
	
	/*Prefixo padrão de Token*/
	private static final String TOKEN_PREFIX = "Bearer";
	
	private static final String HEADER_STRING = "Authorization";
	
	/*Gerando Token de autenticação e adicionando ao cabeçalho e resposta Http*/
	public void addAuthentication(HttpServletResponse response, String username) throws IOException{
		
		/*Montagem do Token*/
		String JWT = Jwts.builder()
				.setSubject(username)
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
				.signWith(SignatureAlgorithm.HS512, SECRET).compact();
		
		/*Junta token com o prefixo*/
		String token = TOKEN_PREFIX + " " + JWT;
		
		/*Adiciona no cabeçalho http*/
		response.addHeader(HEADER_STRING, token);
		
		ApplicationContextLoad.getApplicationContext().getBean(UsuarioRepository.class)
				.atualizaTokenUser(JWT, username);
		
		/*liberando resposta para portas diferentes que usam a API ou caso clientes web*/
		liberacaoCors(response);
		
		/*Escreve token como resposta no corpo http*/
		response.getWriter().write("{\"Authorization\": \""+token+"\"}");
	}
	
	/*Retorna o usuário validado com token ou caso não seja válido, retorna null*/
	public Authentication getAuthentication(HttpServletRequest request, HttpServletResponse response) {

		try {
		/* Pega o token enviado no cabeçalho http */
		String token = request.getHeader(HEADER_STRING);

		if (token != null) {
			
			String tokenLimpo = token.replace(TOKEN_PREFIX, "").trim();

			/* Faz a validação do token do usuário na requisição */
			String user = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(tokenLimpo).getBody()
					.getSubject(); /* Ex: João Silva */

			if (user != null) {

				Usuario usuario = ApplicationContextLoad.getApplicationContext().getBean(UsuarioRepository.class)
						.findUserByLogin(user);

				/* Retorna o usuário logado */
				if (usuario != null) {
						
					if (tokenLimpo.equalsIgnoreCase(usuario.getToken())) {
						
						return new UsernamePasswordAuthenticationToken(usuario.getLogin(), usuario.getSenha(), usuario.getAuthorities());
					}
				}
			}

		} /*fim da condição token*/
		}catch(io.jsonwebtoken.ExpiredJwtException e) {
			try {
				response.getOutputStream().println("Seu TOKEN está expirado, faça o login ou informe um novo TOKEN para autenticação");
			} catch (IOException e1) {}
		}
		
		liberacaoCors(response);
		return null;
	}

	private void liberacaoCors(HttpServletResponse response) {

		if (response.getHeader("Access-Control-Allow-Origin") == null) {
			response.addHeader("Access-Control-Allow-Origin", "*");
		}
		
		if (response.getHeader("Access-Control-Allow-Headers") == null) {
			response.addHeader("Access-Control-Allow-Headers", "*");
		}
		
		if (response.getHeader("Access-Control-Request-Headers") == null) {
			response.addHeader("Access-Control-Request-Headers", "*");
		}
		
		if (response.getHeader("Access-Control-Allow-Methods") == null) {
			response.addHeader("Access-Control-Allow-Methods", "*");
		}
	}
	
}