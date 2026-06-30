# Unomi Helm Chart

This chart deploys the Unomi Modern Spring Boot application.

It expects PostgreSQL, Elasticsearch, Redis, and Kafka to already exist in the Kubernetes cluster or to be provided as managed services. Configure their endpoints in `values.yaml`.

Install:

```bash
helm upgrade --install unomi deployment/helm/unomi \
  --set image.repository=your-registry/unomi-modern \
  --set image.tag=latest
```

Use an existing PostgreSQL credential secret:

```bash
helm upgrade --install unomi deployment/helm/unomi \
  --set env.datasource.existingSecret=unomi-postgres \
  --set env.datasource.usernameKey=username \
  --set env.datasource.passwordKey=password
```

Scale worker-only releases by disabling roles you do not want in each release:

```bash
helm upgrade --install unomi-api deployment/helm/unomi \
  --set env.outbox.publisherEnabled=false \
  --set env.consumers.writeEsEnabled=false \
  --set env.consumers.mergeEnabled=false \
  --set env.consumers.segmentEnabled=false \
  --set env.consumers.ruleEnabled=false \
  --set env.consumers.actionEnabled=false
```
