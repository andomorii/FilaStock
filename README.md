# FilaStock

App Android para controlar o estoque de filamentos de impressão 3D: tipo, cor, marca, acabamento, fotos, gramas restantes, custo e histórico de uso. Funciona offline (os dados ficam no celular).

## O que já faz (versão 0.2.1)

- Cadastro de carretéis: material, cor, marca, peso inicial e atual, preço, observações
- Acabamento (fosca, brilhante, translúcida, fluorescente, neon, silk, duo) e várias fotos por carretel (tiradas na hora pela câmera ou escolhidas da galeria)
- Compartilhar com cliente (WhatsApp etc.): foto + características, sem preço, gramas nem observações
- Lista com busca, filtro por material e resumo do estoque (kg e valor)
- Registrar uso em gramas, com custo calculado e histórico; pesar o carretel para corrigir o peso
- Alerta de estoque baixo (faixa na lista e notificação)
- QR code por carretel: gera a etiqueta para imprimir e lê pela câmera
- Configurações da impressora Bambu Lab: IP, número de série (com leitura de QR), código de acesso e teste de conexão

## Ainda não faz

- Descontar filamento automaticamente a cada impressão (leitura da A1 e do AMS lite)
- Importar o .3mf do Bambu Studio
- Sincronização na nuvem, lista de compras, NFC

## Como gerar o APK (só pelo celular)

1. No repositório, o arquivo `.github/workflows/build.yml` já extrai o zip e compila.
2. Suba o `FilaStock.zip` (Add file → Upload files). Isso dispara a compilação.
3. Na aba **Actions**, baixe **FilaStock-apk** em *Artifacts*, extraia e instale o `app-debug.apk`.

A partir da versão 0.2.0 o APK usa uma chave de assinatura fixa, então as próximas versões instalam por cima da anterior sem perder dados. A transição da 0.1.0 para a 0.2.0 exige desinstalar a antiga uma vez.
