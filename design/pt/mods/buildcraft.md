# BuildCraft

*A fundação da tech clássica: tubos, motores, a quarry e automação com gates. O Logistics já cobre o núcleo de tubos e motores e uma quarry modernizada; a fronteira aberta são fluidos, energia nível combustão e automação com gates.*

**Era de origem:** 1.7.10–1.12.2 (BuildCraft).
**Módulo Logistics:** `logistics-automation` (domínios pipe + power + automation).
**Fase:** 0 (partes feitas) / 1 (lacunas).

Veja [`../principles.md`](../principles.md) para a legenda da tabela. A linhagem de transporte por tubos está detalhada em [`logistics-pipes.md`](logistics-pipes.md); este arquivo foca em motores, fluidos, quarry/automação e gates.

## Tubos

| Feature | What it did (1.7.10–1.12.2) | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
| Tubos de transporte de itens (madeira/pedra/ferro/ouro/diamante/obsidiana/etc.) | Mover/sortear/extrair itens por material | Modernize | Coberto pelo conjunto de tubos de transporte + smart do Logistics com identidade de material vanilla | ✅ Done | `pipe` / transport + smart pipes |
| Tubo de diamante (sorting) | Sorting de itens por lado | Port | Item Filter Pipe | ✅ Done | `pipe` / Item Filter Pipe |
| Tubo de obsidiana (coleta do mundo) | Aspirar itens/entidades derrubados | Port | Vale adicionar — comportamento de vácuo; balancear alcance | — | Fase 1 — pipes |
| Tubos de sabor pedregulho/lápis/quartzo | Variantes de velocidade de roteamento/cor | Modernize | Coberto/absorvido pelos níveis de material atuais + fluido de marcação | ✅ Done | `pipe` / pipes + marcação |
| Tubos de kinesis (energia) | Transportar energia do motor (MJ) | Modernize | Substituído por **cabos** RF (cobre/ouro/ender) | ✅ Done | `power` / cables |
| Tubos de fluido (impermeáveis) | Transportar líquidos | Port | Precisa de uma camada de transporte de fluidos (API de fluidos da plataforma) | — | Fase 1 — fluidos |

## Motores & energia

| Feature | What it did | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
| Motor de Redstone | Energia fraca do redstone; seguro | Port | Implementado; nunca superaquece | ✅ Done | `power` / Redstone Engine |
| Motor Stirling | Queima combustível sólido; energia média; calor | Port | Implementado com estágios de calor | ✅ Done | `power` / Stirling Engine |
| Motor de Combustão | Queima combustível líquido + precisa de resfriamento com água; alta energia; explode se mal gerenciado | Modernize | A grande lacuna de nível de energia. Precisa de combustível líquido + refrigerante; manter a tensão de "gerenciar ou explode" | — | Fase 1 — motores |
| Calor / superaquecimento do motor | Estágios visuais de calor, desligamento seguro vs. explosão | Port | Implementado (COLD→OVERHEAT) | ✅ Done | `power` / engine heat |
| Unidade de energia MJ | Moeda de energia do BuildCraft | Modernize | Padronizado em **RF** via abstração de energia `core.lib` | ✅ Done | `power` / energy API |

## Quarry & automação de fluidos

| Feature | What it did | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
| Quarry | Mineração automática delimitada por frame | Modernize | **Laser Quarry**: delimitada por marcadores, escalada por energia, sem construção de frame monolítica | ✅ Done | `automation` / Laser Quarry |
| Landmarks / marcadores | Definir áreas de trabalho | Modernize | Blocos de marcador (limites solo + conectado) | ✅ Done | `automation` / Marker |
| Bomba | Bombear fluidos do mundo para tubos/tanques | Port | Precisa da camada de fluidos; bombeamento clássico de óleo/água/lava | — | Fase 1 — fluidos |
| Óleo & Refinaria & Combustível | Lagos de óleo → refinar para combustível de motores de combustão | Port | Trazer como está: geração de óleo no mundo → Refinaria → combustível. Se acopla ao Motor de Combustão; biocombustível Forestry é um combustível *paralelo*, não substituto | — | Fase 1 — combustíveis |
| Filler | Preenchimento/limpeza automática de áreas com padrões | Skip | Automação de construção; tedioso, sobrepõe nicho de Create/Schematica | ❌ | — |
| Builder / Architect / Blueprints / Library | Salvar & construir estruturas automaticamente | Skip | Pesado; Schematica-likes modernos cobrem; fora do escopo | ❌ | — |

## Gates, fiação & lógica de automação

| Feature | What it did | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
| Gates (básico/ferro/ouro/diamante) | Lógica programável de trigger→ação em tubos/máquinas | TBD | Alto valor mas complexo. Possível abordagem moderna: um "logic gate" compacto plugável lendo estado de máquina/tubo. Grande esforço de design — precisa de Discussion | — | Fase 1 — automação (RFC) |
| Fiação de tubos (vermelha/azul/verde/amarela) | Carregar sinais de gate entre tubos | TBD | Só faz sentido se gates forem portados; poderia usar redstone vanilla em vez disso | — | Fase 1 — automação (RFC) |
| Gate autárquico | Motor de redstone com auto-pulsação | Modernize | Poderia se integrar à config de motor/extrator em vez de um gate separado | — | — |
| Assembly Table + Laser | Crafting a laser para chipsets/gates do BC | TBD | Revisitar conforme nos aproximamos das receitas que produzia — partes já mudaram, mas o equilíbrio ainda é incerto. Decidir Portar vs. Modernizar mais perto | — | Fase 1 — revisitar |
| Facades | Capas cosméticas sobre tubos | Port | Feature forte — camuflagem de tubos/cabos com aparência de blocos via data component. Agendado **pós-1.0** | — | Pós-1.0 |
| Robôs & estações | Trabalhadores móveis programáveis | Skip | Muito complexo; fora do escopo | ❌ | — |
| Chave inglesa | Configurar/rotacionar máquinas | Port | Implementada | ✅ Done | `core` / Wrench |

> TODO: confirmar o exato comportamento de refrigerante/explosão do motor de combustão que queremos espelhar (resfriado a água vs. simplificado), e decidir fonte de óleo (geração no mundo vs. craftado) — esses dois se acoplam e bloqueiam o épico de fluidos.
> TODO: a questão de gates é a maior decisão de design em aberto do BuildCraft — marcar para Discussion de Ideas/Polls antes de agendar. Veja [RFC 0001 — Comportamento Programável](../rfcs/0001-programmable-behavior.md).
