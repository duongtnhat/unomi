# Deployment

Build and run the full local stack:

```bash
docker compose -f deployment/docker-compose.yml up -d --build
```

The API process is exposed at:

```text
http://localhost:8080
```

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

Spring Boot Admin is available at:

```text
http://localhost:9090
```

The compose stack runs these application roles separately:

```text
api
worker-write-es
worker-merge
worker-segment
worker-rule
worker-action
worker-webhook
worker-email
spring-boot-admin
```

Only `api` exposes port `8080` and runs the outbox publisher. Each worker enables exactly one Kafka consumer and registers itself with Spring Boot Admin through its internal actuator URL.

Stop the stack:

```bash
docker compose -f deployment/docker-compose.yml down
```

Remove persistent volumes:

```bash
docker compose -f deployment/docker-compose.yml down -v
```

Render or install the Kubernetes chart:

```bash
helm template unomi deployment/helm/unomi
helm upgrade --install unomi deployment/helm/unomi
```
