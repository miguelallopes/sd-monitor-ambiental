FROM postgres:18

VOLUME [ "/var/lib/postgresql" ]

EXPOSE 5432/tcp

HEALTHCHECK --interval=10s --timeout=5s --start-period=5s --retries=5 \
    CMD sh -c 'PGPASSWORD="$POSTGRES_PASSWORD" pg_isready -U "$POSTGRES_USER" || exit 1'