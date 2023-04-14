# API Springboot com autenticação JWT e atualização de token.
Este projeto é uma REST API para o projeto Front-End no meu mesmo GitHub chamado "projeto_crud_angular9". As principais tecnologias utilizadas foram Spring Boot, Spring Data JPA, Spring Security, PostgreSQL e JWT.

Por esta API, há autenticação do usuário para o log in do sistema utilizando Json Web Token e Spring Security, assim como a necessidade de um novo log in quando o token de autenticação é expirado (tempo de 2 dias). Há o mapeamento de liberação de CORS e a autenticação da url que o usuário está navegando.

# Login & Recuperação de senha
Caso o usuário esqueça a sua senha de acesso, uma nova senha pode ser enviada para o e-mail. Para isto, o usuário precisa de fornecer o e-mail válido que havia sido cadastrado anteriormente. A senha enviada para o e-mail é a data do dia em que o usuário solicitou o pedido de recuperação de senha.

# Relatório pdf
Esta API implementa um serviço de relatório feito com JasperSoft Studio para a impressão em tela suspensa e a possibilidade de download do mesmo arquivo. Este relatório contém a lista de usuários cadastrados, seus Id's de identificação e data de nascimento.

# Gráfico de usuário com Google Charts
Esta API provê os dados necessários para o carregamento de um gráfico de barras que mostra a relaçao usuário x salário recebido.

# Outras funcionalidades
Além das principais funcionalidades descritas acima, esta API implementa métodos padrões para salvar, pesquisar, editar, deletar ou criar um novo registro de usuário no banco de dados. Há também o carregamento de dados dos registros cadastrados sob demanda exibidos na tela para performance da aplicação na web.
