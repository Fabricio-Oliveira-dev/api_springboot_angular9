package curso.api.rest.service;

import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

@Service
public class ServiceRelatorio implements Serializable{

	private static final long serialVersionUID = 1L;

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/*END-POINT que gera o relatório*/
	public byte[] gerarRelatorio (String nomeRelatorio, Map<String, Object> params, ServletContext servletContext) throws Exception {
		
		/*obtem conexão com o banco de dados*/
		Connection connection = jdbcTemplate.getDataSource().getConnection();
		
		/*Carrega o caminho do arquivo*/
		String caminhoJasper = servletContext.getRealPath("relatorios")
				+ File.separator + nomeRelatorio + ".jasper";
		
		/*Gera o relatório com os dados e conexão*/
		JasperPrint print = JasperFillManager.fillReport(caminhoJasper, params, connection);
		
		/*Exporta em byte o PDF para fazer o download*/
		byte[] retorno = JasperExportManager.exportReportToPdf(print);
		connection.close();
		
		return retorno;
	}
}
