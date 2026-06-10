# Arquitetura: Domínios & Divisão de Módulos

> **Status: provisional.** Este é um alvo para seguir e revisitar, não uma fronteira comprometida. A questão difícil — *onde exatamente ficam as costuras para que cada módulo funcione sozinho?* — ainda está em aberto.

## Hoje: domínios em um jar

O mod atualmente é entregue como um artefato único, organizado internamente em **domínios desacoplados** que dependem apenas de abstrações compartilhadas em `core.lib` (Inversão de Dependência):

```text
common/src/main/java/com/logistics/
├── core/         # lib compartilhada + materiais, ferramentas, Macerador; abstrações core.lib
├── pipe/         # transporte de itens + rede logística (Logistics Pipes)
├── power/        # motores, cabos, bateria, abstração de energia
└── automation/   # Kiln, Laser Quarry, marcadores
```

Domínios não se importam entre si; se comunicam através de `core.lib` (`PlatformService`, lookups de energia/item/fluido, contratos de rede, `DomainBootstrap`, etc.). Esse desacoplamento é o que torna uma futura divisão *possível* — as costuras já existem em grande parte.

## Alvo: uma família pequena de módulos

O objetivo é deixar jogadores instalarem **apenas o que querem** — não um monólito. Recriar conteúdo de cinco mods em um jar gigante prejudicaria a flexibilidade de "instale as partes que quiser" que definia os tech packs clássicos.

Divisão provisional proposta, aproximadamente pelas linhas dos mods de origem:

```text
logistics-core        # biblioteca compartilhada: abstrações core.lib, APIs de energia/item/fluido,
                      #   materiais base (estanho/bronze/pós/engrenagens), ferramentas (chave/sonda).
                      #   Todo outro módulo depende deste; sozinho não entrega nada "divertido".

logistics-automation  # BuildCraft + Logistics Pipes + Thermal Expansion:
                      #   tubos & a rede logística, motores & cabos, máquinas RF
                      #   (macerador, kiln, quarry). O conteúdo "principal" atual.

logistics-forestry    # Forestry: abelhas, árvores, fazendas, tubos eletrônicos, a bancada.

logistics-transport   # Railcraft: trilhos, carts avançados, tanques, sinais, processamento em massa.
```

### Questão em aberto: dividir o `logistics-automation` ainda mais?

`logistics-automation` é o maior balde e é argumentavelmente três mods num casaco. Uma divisão mais fina poderia ser:

```text
logistics-pipes    # transporte + rede logística (a "cola")
logistics-power    # motores, cabos, bateria, distribuição de energia
logistics-machines # máquinas RF: macerador, kiln, quarry, futuras máquinas estilo Thermal
```

**Trade-off:**
- *Divisão mais fina* → máxima escolha do jogador (tubos sem máquinas, etc.), mas mais superfície de API entre módulos para manter estável, e mais difícil "achar as bordas" para que cada um funcione sozinho (ex.: tubos são muito mais úteis *com* energia quando operações custam energia — veja o trabalho de tubos com gate de energia).
- *Divisão mais grossa* (único `logistics-automation`) → bordas mais simples, menos contratos entre módulos, mas menos granularidade de escolha.

**Recomendação provisional:** entregar a divisão grossa primeiro (`core` + `automation` + `forestry` + `transport`), e só separar o `automation` quando as costuras internas do `core.lib` se provarem limpas o suficiente para que tubos/energia/máquinas genuinamente funcionem sozinhos. Decidir via Discussion antes de comprometer.

## Princípios para manter módulos separáveis

- **`logistics-core` detém todo contrato transversal.** Qualquer coisa que dois módulos precisem (unidade de energia, transferência de item/fluido, interfaces do grafo de rede, materiais compartilhados) vive no core. Módulos nunca dependem um do outro diretamente.
- **Cada módulo degrada graciosamente sozinho.** Um módulo deve ser jogável com apenas `logistics-core` presente. Sinergias opcionais entre módulos (ex.: máquinas Forestry aceitando tubos Logistics) são *detectadas*, não *obrigatórias*.
- **Especificidades de loader ficam em adaptadores.** A regra multiloader existente vale em todos os módulos: `common` é agnóstico de loader; a fiação Fabric/NeoForge vive em source sets de loader.
- **Achar as bordas deliberadamente.** Antes de dividir, escrever que API cada lado precisa do outro; se essa superfície for grande ou instável, a costura não está pronta.

## Relação com o build hoje

Nenhuma mudança de empacotamento é necessária para iniciar o roadmap — a estrutura de domínios já espelha o alvo. A divisão se torna uma preocupação de *build/Gradle* (artefatos separados) quando o conteúdo justificar. Até lá, trate os nomes de módulos acima como **rótulos para grupos de domínios**, e mantenha o novo código do lado certo da costura.
