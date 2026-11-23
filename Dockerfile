# Compilation du moteur et création du dictionnaire binaire
FROM php:8.3-apache AS moteur
RUN apt-get update \
    && apt-get install -y --no-install-recommends gcc make libc6-dev \
    && rm -rf /var/lib/apt/lists/*
COPY moteur/ /construction/moteur/
COPY donnees/ /construction/donnees/
RUN make -C /construction/moteur \
    && /construction/moteur/bin/dictionnary_build /construction/donnees/words.txt /construction/donnees/dico.lex

# Serveur web sans les sources du moteur ni les outils de compilation
FROM php:8.3-apache

# Extension PDO PostgreSQL (Neon)
RUN apt-get update \
    && apt-get install -y --no-install-recommends libpq-dev \
    && docker-php-ext-install pdo_pgsql \
    && rm -rf /var/lib/apt/lists/*

# Configuration PHP de production (display_errors désactivé, expose_php off)
RUN mv "$PHP_INI_DIR/php.ini-production" "$PHP_INI_DIR/php.ini"

# Seul le dossier public/ est exposé par Apache
ENV APACHE_DOCUMENT_ROOT=/var/www/html/public
RUN sed -ri -e 's!/var/www/html!${APACHE_DOCUMENT_ROOT}!g' \
        /etc/apache2/sites-available/*.conf \
        /etc/apache2/apache2.conf \
        /etc/apache2/conf-available/*.conf \
    && a2enmod headers \
    && printf 'ServerTokens Prod\nServerSignature Off\n' > /etc/apache2/conf-available/security-hardening.conf \
    && a2enconf security-hardening

# Exécutables et données de jeu, détenus par root
COPY --from=moteur /construction/moteur/bin/grid_build /construction/moteur/bin/grid_path /construction/moteur/bin/dictionnary_lookup /construction/moteur/bin/score /construction/moteur/bin/solve /opt/boogle/moteur/bin/
COPY --from=moteur /construction/donnees/dico.lex /construction/donnees/chartable.txt /opt/boogle/donnees/

# Fichiers du site et dossier de journalisation accessible à Apache
COPY public/ /var/www/html/public/
COPY src/ /var/www/html/src/
COPY templates/ /var/www/html/templates/
RUN install -d -o www-data -g www-data /var/www/html/logs

EXPOSE 80
