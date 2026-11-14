# Recarrega as variáveis de ambiente direto do Windows.
# Motivo: se o VS Code foi aberto ANTES de você configurar PATH, JAVA_HOME etc.,
# ele continua usando os valores antigos. Este script busca os valores atuais.

foreach ($nome in 'JAVA_HOME', 'MAVEN_HOME', 'CATALINA_HOME', 'CATALINA_BASE') {
    $valor = [Environment]::GetEnvironmentVariable($nome, 'Machine')
    if (-not $valor) { $valor = [Environment]::GetEnvironmentVariable($nome, 'User') }
    if ($valor) { Set-Item -Path "Env:$nome" -Value $valor }
}

$env:Path = [Environment]::GetEnvironmentVariable('Path', 'Machine') + ';' +
            [Environment]::GetEnvironmentVariable('Path', 'User')
