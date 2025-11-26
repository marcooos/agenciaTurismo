# Sistema Agência de Turismo – README

Aplicação didática de uma **agência de turismo** feita em **Spring Boot + REST** com **PostgreSQL** e **Front-end HTML/Bootstrap/JS** consumindo as APIs. Inclui **login de vendedores**, **autorização por perfil (ADMIN/VENDEDOR)**, **dashboard**, **CRUDs** (Clientes, Vendedores, Pacotes, Vendas) e **upload opcional de imagem** para pacotes.

---

## Sumário

* [Arquitetura](#arquitetura)
* [Funcionalidades](#funcionalidades)
* [Stack & Requisitos](#stack--requisitos)
* [Configuração](#configuração)

  * [Banco de dados (PostgreSQL)](#banco-de-dados-postgresql)
  * [Propriedades da aplicação](#propriedades-da-aplicação)
  * [Sessão & Cookie](#sessão--cookie)
  * [Uploads de imagem](#uploads-de-imagem)
* [Executando](#executando)
* [Estrutura de Pastas](#estrutura-de-pastas)
* [Segurança](#segurança)
* [API – Endpoints](#api--endpoints)
* [Front-end](#front-end)
* [Exemplos (cURL)](#exemplos-curl)
* [Roadmap de Aula / Exercícios](#roadmap-de-aula--exercícios)
* [Problemas comuns](#problemas-comuns)
* [Licença](#licença)

---

## Arquitetura

* **Backend**: Spring Boot 3, REST Controllers, Spring Security (session-based), validações simples, camada de repositório JPA.
* **Banco**: PostgreSQL (didático), com entidades básicas e relacionamentos simples.
* **Front-end**: HTML estático com **Bootstrap 5** e **JavaScript** (módulos ES) chamando as APIs. Navbar parcial reutilizável (`/partials/navbar.html`) e utilitários centralizados em `/js/app.js`.
* **Autenticação**: Login via `/api/login` (session cookie), `@PreAuthorize` por perfil.
* **Uploads**: Imagens servidas via `/files/**` a partir de um diretório local (`uploads/`).

---

## Funcionalidades

* **Login** de vendedor e **manutenção de sessão** (cookie).
* **Perfis**: `ADMIN` (acesso total) e `VENDEDOR` (operações do dia a dia).
* **Dashboard** (página inicial): contadores de entidades e **faturamento** total (vendas).
* **CRUDs**:

  * **Clientes** (ADMIN/VENDEDOR)
  * **Pacotes** (ADMIN/VENDEDOR) + **upload opcional de imagem**
  * **Vendas** (ADMIN/VENDEDOR)
  * **Vendedores** (somente **ADMIN**; criação com senha `{noop}` automática no didático)
* **Front** com **Bootstrap** e chamadas via `fetch` centralizadas (helpers GET/POST/PUT/DELETE com `credentials:'include'`).

---

## Stack & Requisitos

* **Java 21** (ou compatível com seu Spring Boot)
* **Maven 3.9+**
* **PostgreSQL 14+**
* Navegador moderno

---

## Configuração

### Banco de dados (PostgreSQL)

Crie um banco (ex.: `agencia`) e um usuário com permissão:

```sql
CREATE DATABASE agencia;
CREATE USER agencia_user WITH ENCRYPTED PASSWORD 'agencia_pass';
GRANT ALL PRIVILEGES ON DATABASE agencia TO agencia_user;
```

### Propriedades da aplicação

No `src/main/resources/application.properties` (ajuste conforme seu ambiente):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/agencia
spring.datasource.username=agencia_user
spring.datasource.password=agencia_pass
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Sessão
server.servlet.session.timeout=15m
server.servlet.session.cookie.name=AGENCIASESSION
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=lax
server.servlet.session.cookie.secure=false

# Uploads
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=2MB
spring.servlet.multipart.max-request-size=3MB
app.upload.dir=uploads
```

### Sessão & Cookie

* Autenticação baseada em sessão (`AGENCIASESSION`).
* Security configurado com `requireExplicitSave(false)` para salvar o contexto automaticamente na sessão após login.

### Uploads de imagem

* As imagens são salvas em `${app.upload.dir}/pacotes/` e servidas via `/files/pacotes/{arquivo}`.
* O handler está configurado em `WebConfig.addResourceHandlers`.

---

## Executando

```bash
mvn clean spring-boot:run
```

Acesse:

* **Front**: `http://localhost:8080/index.html`
* **Login**: `http://localhost:8080/login.html`

Dados de exemplo (opcional) podem ser inseridos via `data.sql`, por ex.:

```sql
INSERT INTO vendedor (nome,email,senha,role) VALUES
('Admin','admin@agencia.com','{noop}123','ADMIN'),
('Ana','ana@agencia.com','{noop}123','VENDEDOR');
```

> **Login didático**: `admin@agencia.com / 123` (ADMIN).

---

## Estrutura de Pastas

```
src/
  main/
    java/com/dev/turismo/agencia/
      AgenciaApplication.java
      config/
        WebConfig.java               # /files/** -> uploads/
      controller/
        AuthController.java
        DashboardController.java
        ClienteController.java
        PacoteController.java        # inclui POST /{id}/imagem
        VendaController.java
        VendedorController.java
      model/
        Role.java
        Vendedor.java
        Cliente.java
        Pacote.java                  # campo imagemUrl
        Passagem.java
        Hospedagem.java
        Venda.java
      repository/
        *.java
      security/
        SecurityConfig.java
        VendedorUserDetailsService.java
      service/
        FileStorageService.java
    resources/
      application.properties
    webapp/ (ou static/)
      index.html
      login.html
      clientes.html
      vendedores.html
      pacotes.html                   # input file + preview + upload pós-salvar
      vendas.html
      partials/
        navbar.html
      js/
        app.js                       # helpers fetch + sessão + navbar
```

*(Dependendo do empacotamento, os HTML/JS/CSS podem estar em `src/main/resources/static/`.)*

---

## Segurança

* `@EnableMethodSecurity` habilitado.
* Regras por controller/método com `@PreAuthorize`:

  * **VendedorController**: `hasRole('ADMIN')`
  * **Pacote/Cliente/Venda/Dashboard**: `hasAnyRole('ADMIN','VENDEDOR')`
* Login REST: `POST /api/login` (público); `GET /api/auth/me` (autenticado).
* Logout REST: `POST /api/logout` (204).

---

## API – Endpoints

### Auth

* `POST /api/login` – body: `{"email","senha"}` → 200 com dados do vendedor e cria sessão.
* `GET /api/auth/me` – retorna vendedor logado.
* `POST /api/logout` – encerra a sessão (204).

### Dashboard

* `GET /api/dashboard/resumo` → `{ totalClientes, totalVendedores, totalPacotes, totalVendas, faturamento }`

### Vendedores (ADMIN)

* `GET /api/vendedores`
* `GET /api/vendedores/{id}`
* `POST /api/vendedores` – aceita `{ nome, email, senha, role }` (senha `{noop}` automática se vier sem prefixo).
* `PUT /api/vendedores/{id}` – mantém senha se campo vazio; permite trocar `role`.
* `DELETE /api/vendedores/{id}`

### Clientes / Pacotes / Vendas (ADMIN/VENDEDOR)

* `GET /api/clientes | /api/pacotes | /api/vendas`
* `GET /api/clientes/{id} | /api/pacotes/{id} | /api/vendas/{id}`
* `POST /api/clientes | /api/pacotes | /api/vendas`
* `PUT /api/clientes/{id} | /api/pacotes/{id} | /api/vendas/{id}`
* `DELETE /api/clientes/{id} | /api/pacotes/{id} | /api/vendas/{id}`

### Upload de imagem do Pacote

* `POST /api/pacotes/{id}/imagem` (multipart `form-data`, campo `file`)

  * Salva arquivo e atualiza `imagemUrl`.
  * **PUT /api/pacotes/{id}** não mexe em `imagemUrl` (preserva se não trocar a imagem).

### Arquivos estáticos (público)

* `GET /files/**` – serve arquivos do diretório `uploads/`.

---

## Front-end

* Todas as páginas usam **Bootstrap** e o **navbar parcial** (`/partials/navbar.html`).
* JS centralizado em **`/js/app.js`**:

  * `requestJSON/getJSON/postJSON/putJSON/del` com `credentials:'include'`
  * `getUserSession()/requireAuth()/loadNavbar()/bootPage()`
  * Tratamento de **401** (redirect para login) e **403** (alerta de acesso negado)
* **Pacotes**:

  * Form com input `file` opcional + **pré-visualização**.
  * No **submit**, primeiro `POST/PUT` JSON, depois **upload** se houver arquivo selecionado.
  * A lista exibe **miniatura** quando `imagemUrl` existir.

---

## Exemplos (cURL)

Login e manter cookie:

```bash
# Login (salva cookie)
curl -i -c jar.txt -H "Content-Type: application/json" \
  -d '{"email":"admin@agencia.com","senha":"123"}' \
  http://localhost:8080/api/login

# Sessão atual
curl -b jar.txt http://localhost:8080/api/auth/me
```

Criar pacote e enviar imagem:

```bash
# Criação
curl -b jar.txt -H "Content-Type: application/json" \
  -d '{"titulo":"Foz do Iguaçu","descricaoCurta":"3 noites","precoBase":1990.00,
       "passagem":{"companhia":"LATAM","origem":"GRU","destino":"IGU"},
       "hospedagem":{"hotel":"Hotel Cataratas","cidade":"Foz","noites":3}}' \
  http://localhost:8080/api/pacotes

# Upload de imagem (substitua {id})
curl -b jar.txt -F "file=@/caminho/para/foto.jpg" \
  http://localhost:8080/api/pacotes/{id}/imagem
```

---

## Problemas comuns

* **403 mesmo logado**: o usuário não tem a **role** exigida pelo endpoint; ver `@PreAuthorize`.
* **401 / perde sessão**: conferir `credentials:'include'` no front e cookie `AGENCIASESSION` no navegador.
* **Imagem some ao editar**: garantir que o `PUT` **não** sobrescreve `imagemUrl` com `null`.
* **Upload > 2MB**: ajustar `spring.servlet.multipart.max-file-size`/`max-request-size`.

---

## Licença

Uso **didático**. Adapte livremente para fins educacionais e estudos.

Segue um modelo de `README.md` prontinho para você colocar no GitHub do sistema da agência de turismo, explicando como publicar na AWS usando **Spring Boot + PostgreSQL (RDS) + Elastic Beanstalk**.

Você pode copiar e só ajustar os nomes (app, stack, prints etc.) conforme seu projeto.

---

````markdown
# 🌎 Sistema de Agência de Turismo

Aplicação web desenvolvida em **Java 17 + Spring Boot 3 + Spring Data JPA + Thymeleaf + PostgreSQL**, com foco em gestão de pacotes, clientes e reservas para uma agência de turismo.

Este guia explica **como publicar o sistema na AWS** usando:

- **AWS Elastic Beanstalk** para hospedar a aplicação Spring Boot
- **Amazon RDS (PostgreSQL)** como banco de dados em produção

---

## 🏗️ Arquitetura em Produção (AWS)

- **Elastic Beanstalk (EB)**  
  - Ambiente: Java (Corretto 17)  
  - Deploy: arquivo `.jar` gerado pelo Maven

- **Amazon RDS – PostgreSQL**
  - Banco dedicado à aplicação
  - Acesso restrito ao Security Group do Elastic Beanstalk

- **Amazon S3 (opcional)**
  - Para armazenar arquivos estáticos, backups etc.

---

## ✅ Pré-requisitos

Antes de publicar:

1. **Conta AWS ativa**
2. **Usuário IAM** com permissões para:
   - Elastic Beanstalk
   - RDS
   - EC2
   - S3 (se utilizar)
3. **AWS CLI instalado e configurado** na sua máquina  
   ```bash
   aws configure
   # informe Access Key, Secret, região e formato de saída
````

4. **Java 17** instalado
5. **Maven** instalado e configurado
6. Projeto Spring Boot rodando localmente (por exemplo):

   ```bash
   mvn spring-boot:run
   ```

---

## ⚙️ Configurações do Projeto

### 1. `pom.xml`

Garanta que o projeto está configurado com:

* Java 17
* Spring Boot 3.x
* Dependências para Web, JPA e PostgreSQL

---

### 2. Configurações de Banco por Ambiente

Use **variáveis de ambiente** em produção e deixe o `application.properties` preparado:

```properties
# application.properties (padrão - pode ser para DEV)

spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/agencia}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Na AWS, você vai configurar `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` no ambiente do Elastic Beanstalk.

---

## 🗄️ Criando o Banco de Dados no Amazon RDS (PostgreSQL)

1. No console AWS, acesse **RDS → Databases → Create database**.
2. Selecione:

   * **Engine**: PostgreSQL
   * **Template**: Free tier (se aplicável)
3. Defina:

   * `DB instance identifier`: `agencia-turismo-db`
   * `Master username`: `agencia_user`
   * `Master password`: (salve em local seguro)
4. Em **Connectivity**:

   * Escolha a VPC padrão (ou uma VPC específica, se você tiver)
   * Defina um **Security Group** que permita acesso **apenas** do Elastic Beanstalk.
5. Finalize a criação e aguarde o status `Available`.
6. Anote:

   * **Endpoint** (ex.: `agencia-turismo-db.xxxxxxxx.region.rds.amazonaws.com`)
   * **Port** (padrão: 5432)
   * **Database name** (se você definiu um na criação)

Sua `DB_URL` ficará algo como:

```text
jdbc:postgresql://agencia-turismo-db.xxxxxxxx.region.rds.amazonaws.com:5432/agencia
```

---

## 📦 Build da Aplicação (JAR)

No diretório do projeto, gere o `.jar`:

```bash
mvn clean package -DskipTests
```

O artefato final costuma ficar em:

```text
target/agencia-turismo-0.0.1-SNAPSHOT.jar
```

Use esse arquivo no deploy.

---

## ☁️ Criando o Ambiente no Elastic Beanstalk

1. No console AWS, acesse **Elastic Beanstalk**.
2. Clique em **Create application**.
3. Preencha:

   * **Application name**: `agencia-turismo`
4. Em **Platform**:

   * Platform: **Java**
   * Platform branch: **Corretto 17** (ou Java 17 equivalente)
5. Em **Application code**:

   * Escolha **Upload your code**
   * Envie o `.jar` gerado pelo Maven
6. Clique em **Create application** e aguarde a criação do ambiente.

Ao final, você terá uma URL do tipo:

```text
http://agencia-turismo-env.XXXXXXXXXX.region.elasticbeanstalk.com
```

---

## 🔐 Variáveis de Ambiente no Elastic Beanstalk

Para conectar no RDS:

1. Abra o ambiente criado no Elastic Beanstalk.

2. Vá em **Configuration → Software** (ou “Edit” em Software).

3. Em **Environment properties**, adicione:

   ```text
   DB_URL      = jdbc:postgresql://<endpoint-rds>:5432/<nome-banco>
   DB_USERNAME = <usuario>
   DB_PASSWORD = <senha>
   ```

4. Salve as alterações.
   O Beanstalk fará um **redeploy** com essas variáveis.

---

## 🔄 Atualizando o Deploy (Novas Versões)

Sempre que fizer ajustes no sistema:

1. Gere um novo `.jar`:

   ```bash
   mvn clean package -DskipTests
   ```
2. No Elastic Beanstalk:

   * Abra o ambiente
   * Clique em **Upload and deploy**
   * Envie o novo `.jar`
3. Aguarde até o status ficar como **OK**.

---

## ✅ Checklist Rápido de Publicação

1. Projeto compila localmente com `mvn clean package`
2. Banco criado no **RDS PostgreSQL**
3. Security Groups configurados (EB consegue acessar o RDS)
4. Ambiente Java 17 criado no Elastic Beanstalk
5. Variáveis de ambiente `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` configuradas
6. Deploy do `.jar` realizado com sucesso
7. Acesso via URL pública do Elastic Beanstalk

---

## 🧪 Testando em Produção

* Acesse a URL do Elastic Beanstalk no navegador
* Valide:

  * Página inicial carregando
  * Listagem de pacotes, reservas, clientes
  * Cadastro/edição de dados
* Verifique logs em:

  * **Elastic Beanstalk → Logs**
  * E, se necessário, via **CloudWatch Logs**

---

## 🛡️ Boas Práticas (Próximos Passos)

* Usar **Secrets Manager** ou **SSM Parameter Store** para senhas
* Configurar HTTPS com **AWS Certificate Manager + Load Balancer**
* Criar **backup automático** do RDS
* Monitorar métricas no **CloudWatch**
* Utilizar **pipelines de CI/CD** (GitHub Actions → Elastic Beanstalk)

---

## 📚 Referências Úteis

* Documentação Spring Boot: [https://spring.io/projects/spring-boot](https://spring.io/projects/spring-boot)
* Elastic Beanstalk (Java): [https://docs.aws.amazon.com/elasticbeanstalk/](https://docs.aws.amazon.com/elasticbeanstalk/)
* Amazon RDS (PostgreSQL): [https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/USER_PostgreSQL.html](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/USER_PostgreSQL.html)

---
