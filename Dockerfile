FROM maven:3.9-eclipse-temurin-21

WORKDIR /app

# Copiar archivos del proyecto
COPY . .

# Compilar la librería (Maven)
RUN mvn clean package -DskipTests

# Ejecutar clase de ejemplos
CMD ["mvn", "exec:java", "-Dexec.mainClass=org.notification.Main"]
