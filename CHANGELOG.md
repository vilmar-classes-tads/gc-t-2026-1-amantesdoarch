# Changelog

## [0.1.0] - 2026-06-01

### Added

#### Infraestrutura

* Adicionado suporte ao Maven através do arquivo `pom.xml`.
* Configurado o Spring Boot como framework principal da aplicação.
* Configurado banco de dados H2 para desenvolvimento.
* Configurado Spring Security para autenticação e autorização.

#### Módulo de Usuários

* Criada a entidade `User`.
* Criada a entidade `Perfil`.
* Implementados repositórios para persistência de usuários e perfis.
* Implementada camada de serviços para validação das regras de negócio.
* Criado endpoint para cadastro de novos usuários.
* Estruturadas camadas DTO, Entity, Service, Controller e Config.

#### Módulo de Editais

* Criada a entidade `Edital`.
* Criado DTO para transferência de dados de editais.
* Implementado repositório JPA para persistência de editais.
* Implementada camada de serviço para criação, edição e listagem de editais.
* Criado controller REST para gerenciamento de editais.

### Validation Rules

* Validação para garantir que a data de fim de submissão seja posterior à data de início de submissão.
* Validação para garantir que a data de fim de avaliação seja posterior à data de início de avaliação.
* Validação para garantir que o período de avaliação inicie após o encerramento do período de submissão.

### API Endpoints

#### Usuários

* Endpoint para cadastro de novos usuários.

#### Editais

* `GET /api/edital` — listar editais.
* `POST /api/edital` — criar edital.
* `PUT /api/edital/{id}` — editar edital.

### Contributors

* @sauloocavalcante
* @betosilvaz
* @hrss3-dot
