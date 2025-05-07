# Pedido Service

Microserviço responsável por receber a criação de pedidos, persistir no PostgreSQL e publicar eventos de `PedidoCriado` no RabbitMQ.

---

## Tecnologias

- Java 17  
- Spring Boot 3.4.5  
- Spring Data JDBC 
- RabbitMQ (`spring-boot-starter-amqp`)  
- PostgreSQL  
- Maven

---

## Pré-requisitos

- JDK 17  
- Maven  
- RabbitMQ (p.ex. docker-compose)  
- PostgreSQL (p.ex. docker-compose)  

---

## Como rodar localmente

1. Clone este repositório  
   git clone https://github.com/Augusto-Pellizzari/act-pedido-service.git  
   cd act-pedido-service

2. Ajuste as variáveis de ambiente (ou application.yml):

SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/loja  
SPRING_DATASOURCE_USERNAME=postgres  
SPRING_DATASOURCE_PASSWORD=123456  

SPRING_RABBITMQ_HOST=<host>  
SPRING_RABBITMQ_PORT=5672  
SPRING_RABBITMQ_USERNAME=guest  
SPRING_RABBITMQ_PASSWORD=guest

3. Gere o JAR via Maven  
   mvn clean package -DskipTests

4. Execute a aplicação  
   java -jar target/loja-online-pedido-be-0.0.1-SNAPSHOT.jar

5. Ou, se preferir, crie um contêiner Docker:

# Dockerfile (já incluso neste projeto)
FROM maven:3.9.4-eclipse-temurin-17 AS builder  
WORKDIR /build  
COPY pom.xml .  
COPY src ./src  
RUN mvn clean package -DskipTests  

FROM eclipse-temurin:17-jre  
WORKDIR /app  
COPY --from=builder /build/target/*-SNAPSHOT.jar app.jar  
ENTRYPOINT ["java","-jar","app.jar"]

---

## Endpoints

### 1. Criar pedido

POST /api/pedidos  
Content-Type: application/json

{
  "cliente": "Nome do Cliente"
}

**Resposta 200 OK**

{
  "id": 1,
  "cliente": "Nome do Cliente",
  "status": "AGUARDANDO_PAGAMENTO",
  "dataCriacao": "2025-05-07T12:34:56"
}

### 2. Listar todos

GET /api/pedidos

**Resposta 200 OK**

[
  {
    "id": 1,
    "cliente": "Nome do Cliente",
    "status": "AGUARDANDO_PAGAMENTO",
    "dataCriacao": "2025-05-07T12:34:56"
  }
]

### 3. Atualizar status

PUT /api/pedidos/{id}/status?status={StatusPedido}  
Status pode ser AGUARDANDO_PAGAMENTO, PAGO, RECUSADO, etc.

**Resposta 200 OK com o pedido atualizado.**

---

## Fluxo de mensageria

- Ao criar um pedido, o serviço publica um PedidoCriadoEvent no exchange `pedido.criado.exchange`  
- O pagamento-service consome esse evento e processa o pagamento  
- Após confirmação ou recusa, o pagamento-service publica PagamentoConfirmadoEvent no exchange `pagamento.confirmado.exchange`  
- Este serviço consome o evento e atualiza o status do pedido

---

## Tratamento de erros

- Lança BusinessException para erros de domínio  
- Handler global (@RestControllerAdvice) para transformar em payload JSON padronizado

---

## Observações

Para testes locais recomendo usar o docker-compose:  
https://github.com/Augusto-Pellizzari/infrastructure-docker-compose
