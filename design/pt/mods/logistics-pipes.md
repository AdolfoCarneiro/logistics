# Logistics Pipes

*A "cola" de uma base tech clássica — logística de rede com request/provider em camadas sobre os tubos do BuildCraft. No Logistics este é o sistema mais completo: todo o modelo de tubo em três camadas e a rede estão implementados.*

**Era de origem:** 1.7.10 (Logistics Pipes para BuildCraft).
**Módulo Logistics:** `logistics-automation` (domínio pipe) · código em `common/src/main/java/com/logistics/pipe/`.
**Fase:** 0 (Fundação) — amplamente ✅ feito.

Veja [`../principles.md`](../principles.md) para a legenda da tabela.

## Tubos de logística de rede

| Feature | What it did (1.7.10) | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
| Basic Logistics Pipe | Espinha dorsal da rede; roteia itens endereçados, filtragem opcional | Port | Implementado com filtragem + rota padrão | ✅ Done | `pipe` / Basic Logistics Pipe |
| Request Pipe | Puxar itens específicos da rede sob demanda | Port | Tubo requester + tela | ✅ Done | `pipe` / Requester Logistics Pipe |
| Provider Pipe | Anunciar conteúdo de inventário adjacente para a rede | Port | Tubo provider (com suporte a energia) | ✅ Done | `pipe` / Provider Logistics Pipe |
| Supplier Pipe | Manter inventário alvo abastecido nos níveis configurados | Port | Tubo supplier + módulos supplier passivo/ativo | ✅ Done | `pipe` / Supplier Logistics Pipe |
| Crafting Pipe | Autocrafting sob demanda cumprido pela rede | Modernize | Reutiliza o **Crafter vanilla** como bloco de crafting em vez da assembly table do BC | ✅ Done | `pipe` / Crafting Logistics Pipe |
| Satellite Pipe | Endpoint nomeado para roteamento endereçado | Port | Tubo satellite + roteamento nomeado | ✅ Done | `pipe` / Satellite Logistics Pipe |
| Chassis Pipe (Mk1–5) | Tubo modular com N slots de módulo | Port | Chassis MkI–V (1/2/3/4/8 slots) | ✅ Done | `pipe` / Chassis MkI–V |
| Roteamento de crafting de processo/request | Rotear crafting em andamento para máquinas dedicadas | Port | Process Logistics Pipe | ✅ Done | `pipe` / Process Logistics Pipe |

## Módulos (para chassis pipes)

| Feature | What it did | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
| ItemSink / rota padrão | Aceitar overflow / itens não endereçados | Port | Sink + Polymorphic + Enchantment + Mod sinks | ✅ Done | `pipe` / Sink modules |
| Módulo provider (níveis) | Anunciar inventário em níveis | Port | Provider I / II | ✅ Done | `pipe` / Provider modules |
| Módulo extractor (níveis) | Puxar de inventário adjacente em velocidades | Port | Básico / MkII / Avançado (MkIII) | ✅ Done | `pipe` / Extractor modules |
| Módulo supplier (passivo/ativo) | Empurrar estoque para requesters | Port | Supplier passivo + ativo | ✅ Done | `pipe` / Supplier modules |
| Módulo crafter (níveis) | Cumprir crafting em velocidades/paralelismo | Port | Crafter I / II / III | ✅ Done | `pipe` / Crafter modules |
| Módulo QuickSort | Rotear itens por tipo para destinos classificados | Port | Módulo Quicksort | ✅ Done | `pipe` / QuickSort module |
| Módulo Terminus | Endpoint terminal; parar roteamento além dele | Port | Módulo Terminus (slots de cor) | ✅ Done | `pipe` / Terminus module |

## Tubos mecânicos / smart (base adjacente ao BuildCraft)

*As camadas de transporte sobre as quais o Logistics Pipes se assentava. Rastreado aqui porque fazem parte do mesmo domínio; linhagem de tubos BuildCraft está em [`buildcraft.md`](buildcraft.md).*

| Feature | What it did | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
| Tubos de transporte | Mover itens, roteamento aleatório | Modernize | Tubos de transporte de pedra + cobre (identidade de material) | ✅ Done | `pipe` / transport pipes |
| Tubo extrator | Puxar de inventários | Port | Item Extractor Pipe (com gate de energia) | ✅ Done | `pipe` / Item Extractor Pipe |
| Merger / passthrough / void | Combinar / só-tubo / deletar | Port | Merger, Passthrough, Void pipes | ✅ Done | `pipe` / smart pipes |
| Tubo filter / insertion | Roteamento ciente de itens | Port | Item Filter Pipe, Item Insertion Pipe | ✅ Done | `pipe` / smart pipes |
| Speed boost (ouro) | Aceleração com redstone | Port | Golden Transport Pipe | ✅ Done | `pipe` / Golden Transport Pipe |
| Marcação de tubo / cor | Agrupar redes visualmente | Modernize | Fluido de marcação (16 cores de tinta) + sabor de oxidação do cobre | ✅ Done | `pipe` / marking module |

## Lacunas & não portados ainda

| Feature | What it did | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
| Remote Orderer | Interface de mão para requisitar da rede em qualquer lugar no alcance | Modernize | Um item de acesso portátil (tipo sonda) abrindo a UI de requisição; balancear alcance/custo | 🚧 Planned | Fase 1 — logistics QoL |
| Gate de energia para operações | Operações LP precisavam de energia (Power Junction / supplier) | Port | Feito — operações de tubo consomem RF, com sourcing de energia de rede + serviço compartilhado de push de energia; tubos renderizam verde/vermelho por estado de energia (#464, #465, #469) | ✅ Done | `power` / pipe power gate |
| Logística de fluidos | Supplier/provider/request de líquidos pela rede | Modernize | Depende dos tubos de fluido (veja [`buildcraft.md`](buildcraft.md) / [`thermal-expansion.md`](thermal-expansion.md)) | — | Fase 1 — fluidos |
| Firewall pipe | Isolar/segmentar uma sub-rede | Port | Bloco de segmentação de rede; útil para bases grandes | — | Fase 1 — logistics avançado |
| Disco de logística / gerenciamento de rede | Salvar/carregar config de rede, nomenclatura | Modernize | Item de config baseado em data component; revisitar necessidade | — | — |
| Security station | Permissões de rede por jogador | Skip | Pesado, nicho; fora do escopo por ora | ❌ | — |

> TODO: confirmar se o comportamento original de "rota padrão" + prioridade de sink corresponde totalmente ao modelo de prioridade do módulo Sink atual, ou se algum caso de borda difere.
