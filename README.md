# FilaStock

App Android para controlar o estoque de filamentos de impressão 3D: tipo, cor, marca, gramas restantes, custo e histórico de uso. Funciona offline (os dados ficam no celular).

## Etapa 1 (esta versão)

- Cadastro de carretéis: material, cor, marca, peso inicial e atual, preço, observações
- Lista com busca, filtro por material e resumo do estoque (kg e valor)
- Registrar uso em gramas, com custo calculado e histórico
- Pesar o carretel na balança para corrigir o peso restante
- Alerta de estoque baixo (faixa na lista e notificação quando o carretel cruza o limite)
- QR code por carretel: o app gera a etiqueta para imprimir e lê o QR pela câmera

## Como gerar o APK

1. Suba todos os arquivos deste projeto para o repositório (incluindo a pasta oculta `.github`).
2. No GitHub, abra a aba **Actions**. O workflow **Build APK** roda sozinho a cada envio (ou use **Run workflow**).
3. Quando terminar (uns 5 a 10 minutos), abra a execução e baixe **FilaStock-apk** em *Artifacts*.
4. Extraia o .zip, copie o `app-debug.apk` para o celular e instale (o Android pede para permitir instalação de fontes desconhecidas).

## Próximas etapas

2. Importar o arquivo .3mf do Bambu Studio e descontar os gramas por cor
3. Conexão com a Bambu Lab A1 (modo LAN) e leitura dos slots do AMS lite
4. Sincronização na nuvem, lista de compras e NFC
