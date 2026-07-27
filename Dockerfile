# ----------------------------------------------
# ÉTAPE 1 : BUILD (avec Maven et JDK 21)
# ----------------------------------------------
# On utilise une image officielle qui contient Maven et JDK 21
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Définit le répertoire de travail dans le conteneur
WORKDIR /app

# 1. On copie d'abord le fichier pom.xml (pour profiter du cache Docker)
#    Si le pom.xml ne change pas, Docker réutilise le cache pour les dépendances.
COPY pom.xml .

# 2. Téléchargement des dépendances Maven (hors ligne pour accélérer le build)
RUN mvn dependency:go-offline

# 3. On copie le code source
COPY src ./src

# 4. On exécute le package (compile, test, et génère le JAR)
#    On saute les tests ici car on les a déjà lancés dans la CI/CD.
RUN mvn clean package -DskipTests

# ----------------------------------------------
# ÉTAPE 2 : RUNTIME (JRE 21 léger)
# ----------------------------------------------
# On utilise une image slim (Alpine Linux + JRE 21) pour réduire la taille
FROM eclipse-temurin:21-jre-alpine AS runtime

# On définit un utilisateur non-root (sécurité !)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Répertoire de travail
WORKDIR /app

# On copie le JAR généré depuis l'étape "build"
# Le chemin : /app/target/ecommerce-backend-0.0.1-SNAPSHOT.jar
COPY --from=build /app/target/*.jar app.jar

# On expose le port 8080 (celui de notre API)
EXPOSE 8080

# Point d'entrée : lance le JAR
ENTRYPOINT ["java", "-jar", "app.jar"]