# Thermal Expansion

*Máquinas RF e processamento de minérios — a espinha dorsal do mid-game clássico. O Logistics tem o pulverizador (Macerador) e o forno (Kiln); as lacunas são as máquinas de aliagem/serraria/fluidos, saídas secundárias de minérios, upgrades de máquinas e uma linha de armazenamento de energia em camadas.*

**Era de origem:** 1.7.10–1.12.2 (Thermal Expansion / CoFH).
**Módulo Logistics:** `logistics-automation` (domínios automation + power) + materiais base em `logistics-core`.
**Fase:** 0 (partes feitas) / 1 (lacunas).

Veja [`../principles.md`](../principles.md) para a legenda da tabela.

## Máquinas principais

| Feature | What it did (1.7.10–1.12.2) | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
| Pulverizador | Minério → 2 pós + secundário por chance | Modernize | **Macerador** (moagem RF, receitas customizadas, JEI). *Saídas secundárias/subprodutos ainda não implementadas* | ✅ Done | `core` / Macerator |
| — saídas secundárias/subprodutos | Pó bônus por chance por receita | Modernize | Adicionar segunda saída baseada em chance (estilo GT); alto valor, balanceia rendimento de minério | 🚧 Planned | Fase 1 — saídas do macerador |
| Redstone Furnace | Fundição com RF | Modernize | **Kiln** (reutiliza receitas de fundição vanilla) | ✅ Done | `automation` / Kiln |
| Serraria | Toras → pranchas extras + serragem | Port | Já temos pós de Wood Pulp/Flour; precisa da máquina | 🚧 Planned | Fase 1 — Sawmill |
| Induction Smelter | Aliagem: 2 entradas → liga (+ escória) | Port | Máquina chave — torna Bronze/Invar/Electrum etc. coerente; combina com o conjunto de materiais de ligas | 🚧 Planned | Fase 1 — Alloy Smelter |
| Magma Crucible | Sólidos → fluido derretido | Port | Depende da camada de fluidos | — | Fase 1 — fluidos |
| Fluid Transposer | Encher/esvaziar recipientes; receitas fluido+item | Port | Depende da camada de fluidos | — | Fase 1 — fluidos |
| Cyclic Assembler | Autocrafting de máquina | Skip | Coberto pelo Crafter vanilla + Crafting Logistics Pipe | ❌ | [`logistics-pipes.md`](logistics-pipes.md) |
| Phytogenic Insolator | RF + fertilizante → cultivar plantas/árvores | Modernize | Sobrepõe fazendas Forestry; adiar e unificar lá | — | Fase 2 — fazendas Forestry |
| Energetic Infuser / Charge Bench | Carregar itens com energia | TBD | Só se adicionarmos itens portáteis com energia | — | — |
| Glacial Precipitator / Aqueous Accumulator | Fazer gelo/neve / água | TBD | Utilidade menor; baixa prioridade | — | — |

## Upgrades & augments de máquinas

| Feature | What it did | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
| Augments (velocidade / eficiência / secundário / saída automática) | Modificadores de máquina inseridos em slots | Modernize | Sistema forte que vale adotar como mecânica unificada de **upgrade de máquina** no Macerador/Kiln/etc. | 🚧 Planned | Fase 1 — upgrades de máquinas |
| Frames / níveis de máquina (Básico→Resonant) | Níveis craftados bloqueando poder da máquina | Modernize | Mapear na escada de metais vanilla; manter contagem de níveis modesta | — | Fase 1 — níveis de máquina |

## Geração & armazenamento de energia (Dínamos / Energy Cells)

| Feature | What it did | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
| Dínamos (Vapor/Magmático/Compressão/Reativo/etc.) | Geradores RF modulares de vários combustíveis | Modernize | Sobrepõe motores BuildCraft — **unificar** na linha de motores em vez de uma linha de dínamos paralela; magmático/compressão = níveis de motor com combustível líquido | — | Fase 1 — motores |
| Energy Cells (Leadstone→Resonant) | Armazenamento RF em camadas com I/O configurável | Modernize | **Bateria** existe; expandir em uma linha de armazenamento em camadas com config de I/O | 🚧 Planned | `power` / Battery (tiers) |
| Condutores de energia (Fluxducts) | Transporte de RF | Modernize | Coberto por **cabos** (cobre/ouro/ender) | ✅ Done | `power` / cables |
| Itemducts / Fluiducts (+ servo/filtro/retriever) | Rede de transporte de item/fluido | Skip | Coberto pelo sistema de tubos do Logistics | ❌ | [`logistics-pipes.md`](logistics-pipes.md) |
| Tesseract | Item/fluido/energia sem fio entre dimensões | Skip | Fora do escopo (estilo Ender-Storage); substitutos modernos existem | ❌ | — |

## Materiais

| Feature | What it did | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
| Metais base (cobre/estanho/prata/chumbo/níquel) | Minério + lingotes alimentando ligas | Modernize | Cobre é vanilla agora; **Estanho** implementado; adicionar outros apenas conforme ligas exigirem — manter o conjunto pequeno | 🚧 Planned | `core` / materiais |
| Ligas (bronze/invar/electrum/constantan) | Ligas do smelter para níveis | Port | **Bronze** feito; adicionar um conjunto enxuto ligado ao Alloy Smelter | 🚧 Planned | `core` / Bronze (+ ligas) |
| Ligas de alto nível (signalum/lumium/enderium) | Bloquear os níveis superiores do TE | TBD | Só se níveis de máquina de endgame precisarem delas; risco de inchaço | — | — |
| Engrenagens / chapas / componentes | Intermediários de crafting | Modernize | **Engrenagens** (9), pós, logic chips, valves, cores implementados | ✅ Done | `core` / components |
| Vidro endurecido / Rockwool (16 cores) | Decorativo/utilitário | Skip | Inchaço decorativo; fora do escopo | ❌ | — |
| Borracha curada | Componente para cabos/máquinas | Port | **Rubber Chunk / Rubber Mix** implementado | ✅ Done | `power` / rubber |
| Strongboxes / Caches / Portable Tanks / Satchel | Armazenamento portátil | Skip | Armazenamento é trabalho de outros mods (Drawers/Iron Chests) | ❌ | — |

> TODO: confirmar as taxas exatas de saída secundária do pulverizador que queremos espelhar para equilíbrio (TE usava chance por receita + um item secundário) — se conecta ao épico de saídas do macerador.
> TODO: decidir se os dínamos colapsam totalmente na linha de motores ou se mantêm alguns blocos geradores distintos (ex.: um gerador "magmático" de combustível líquido) — se acopla à decisão do motor de combustão do BuildCraft.
