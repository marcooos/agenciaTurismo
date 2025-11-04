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
