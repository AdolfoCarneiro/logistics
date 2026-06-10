# Fundação de Fluidos

> **Status:** 🚧 Planned — **design resolvido, pronto para construir** (keystone) · **Phase:** 1 — Automation core · **Module:** `logistics-automation` (novo domínio `fluid`)
> **Source:** [`../mods/buildcraft.md`](../mods/buildcraft.md) (tubos impermeáveis, bomba), [`../mods/thermal-expansion.md`](../mods/thermal-expansion.md) (magma crucible, transposer) · **Depends on:** nada (a camada de plataforma já existe)
> **Maps to (roadmap):** Fase 1 — 🔑 Fundação de Fluidos · **Unblocks:** Motor de Combustão, Bomba, Magma Crucible, Fluid Transposer, logística de fluidos, envase de biocombustível (Fase 2), carts de tanque/tanques (Fase 3)

O item de maior alavancagem única na Fase 1. Aproximadamente metade do trabalho restante das Fases 1/2/3 está bloqueado no manuseio de fluidos. **A abstração de plataforma já está construída e conectada** — esta feature é o *conteúdo* que se assenta sobre ela: um tubo de transporte de fluido, um bloco de tanque e um contrato para I/O de fluido em máquinas.

## Reality check: o que já existe

A parte difícil e específica de loader está **feita**. Verificado no codebase:

| Camada | Classe(s) | Localização | Estado |
|---|---|---|---|
| Contrato de armazenamento central | `IFluidStorage` (insert/extract/contents, semântica simulate) | `core/lib/fluids/` | ✅ Implementado |
| Identidade de fluido | `IFluidKey`, `IFluidView` | `core/lib/fluids/` | ✅ Implementado |
| Tanque pronto para uso | `FluidTankComponent` (variante única, persistente em NBT) | `core/lib/fluids/` | ✅ Usável como está |
| Marcador de capability de BE | `HasFluidStorage` (`fluidStorage(side)`) | `core/lib/block/capability/` | ✅ Implementado |
| SPI de lookup de vizinho | `FluidStorageLookup.find(level, pos, dir)` | `core/lib/fluids/` | ✅ Conectado em ambos os loaders |
| Adapter Fabric | `FabricFluidStorage`, `FabricFluidKey`, `FluidStorageAccess` (fallback SIDED) | `fabric/.../fluids`, `fabric/.../capability` | ✅ Implementado + registrado |
| Adapter NeoForge | `NeoForgeFluidStorage`, `NeoForgeFluidKey` | `neoforge/.../fluids` | ✅ Implementado |
| Wiring de máquina NeoForge | `NeoForgeCapabilityRegistration.registerFluids(event, type)` | `neoforge/...` | ⚠️ Template presente, atualmente comentado — descomentar + chamar por tipo de BE de fluido |

Isso espelha a stack de armazenamento de item 1:1 (`IItemStorage`/`HasItemStorage`/`ItemStorageLookup` → `IFluidStorage`/`HasFluidStorage`/`FluidStorageLookup`). É o **mesmo padrão que Team Reborn Energy**: código comum fala com um SPI; cada loader fornece a bridge nativa (Fabric Transfer API `Storage<FluidVariant>`, NeoForge `ResourceHandler<FluidResource>`). Nenhuma nova dependência externa é necessária.

> **Nota de precisão de doc:** o roadmap e os detalhamentos do BuildCraft/TE descrevem fluidos como "precisa de uma camada de transporte de fluido (API de fluido de plataforma)." Essa camada de plataforma já está presente. Atualizar essas linhas de "precisa da camada de fluido" para "precisa de *conteúdo* de fluido (tubos/tanques); camada de plataforma feita" quando esta feature for agendada.

## Problem & goal

O Logistics move itens e energia mas não pode mover ou armazenar **fluidos**. Isso bloqueia o tier de energia de combustível líquido inteiro (motores de Combustão/Magnético), extração de fluido do mundo (Bomba), várias máquinas Thermal (Magma Crucible, Fluid Transposer), logística de fluidos na rede e features downstream de fluido do Forestry/Railcraft.

**Goal:** entregar uma fatia mínima e completa de manuseio de fluidos — *mover, armazenar e alimentar fluidos em máquinas* — que features posteriores consumam sem trabalho de plataforma adicional. Combinar o feel visual e de identidade de material dos tubos de item.

## Requirements

### Functional
- **Tubo(s) de transporte de fluido** — tubos de fluido standalone com fluxo de pressão/equalização simples (estilo tubo impermeável do BuildCraft), carregando **um tipo de fluido por linha — sem mistura** (uma linha carregando água não aceitará lava até drenar; portar o comportamento do BC). Conectar a block entities `HasFluidStorage` adjacentes (qualquer inventário de fluido de mod via `FluidStorageLookup`) e entre si. **Set de fundação:** um **tubo base** (10 mB/t, passivo gratuito), um **tier Dourado** (80 mB/t, passivo gratuito), um tubo **extrator** com gate de energia e um tubo **void** (veja o roster no Design sketch).
- **Bloco de tanque** — um buffer apoiado em `FluidTankComponent`, 16.000 mB (16 baldes). Armazena uma variante de fluido; **insert/extract em todas as faces** (plano — config por face é uma preocupação posterior); **empilha verticalmente e mescla** em um único reservatório lógico (enche de baixo para cima, comportamento do `TileTank` do BC). Nível de preenchimento + fluido mostrado por um **renderer dinâmico de block-entity** (sprite de fluido real na altura), não estágios de block-state.
- **Contrato de I/O de fluido para máquinas** — máquinas que contêm fluido implementam `HasFluidStorage`; o template `registerFluids(...)` do NeoForge é habilitado e chamado para cada tipo de BE de fluido (Fabric auto-descobre via o fallback SIDED de `FluidStorageAccess`).
- **Interop com containers** — baldes (e idealmente qualquer item container de fluido) podem encher/drenar um tanque à mão. (O Fluid Transposer automatiza isso depois; suporte manual a balde é o mínimo.)
- **Primeiro consumidor = Bomba** *(fatia de validação fina, pode ser seu próprio doc/PR)* — um bloco que extrai um bloco fonte de fluido do mundo para um tanque/tubo de fluido adjacente, provando a cadeia inteira de ponta a ponta.

### Equilíbrio — ancorado no BuildCraft

Começar dos números reais do BuildCraft (ele *é* a linhagem) e ajustar a partir daí, em vez de inventar taxas. Referência: **BuildCraft 7.1.27 / MC 1.7.10** (checkout `../buildcraft`). Unidade: **1 balde = 1000 mB** — vanilla e BC concordam, então esta é a unidade base em tudo.

**Throughput de tubo.** O BC deriva a taxa de cada tubo de fluido de uma única **taxa de fluxo base (padrão 10 mB/t)** vezes um multiplicador por tubo (`PipeTransportFluids` `fluidCapacities`):

| Tubo(s) de fluido BC | Multiplicador | Taxa @ base 10 | Papel |
|---|---|---|---|
| Pedregulho · Madeira · Void | 1× | 10 mB/t | base / extrator / void |
| Pedra · Arenito | 2× | 20 mB/t | médio |
| Ferro · Argila · Quartzo · Esmeralda | 4× | 40 mB/t | alto / roteamento / extração-filtrada |
| Diamante · Ouro | 8× | 80 mB/t | sorting / velocidade-máxima |

(Buffer interno por tubo = `25 × base` = 250 mB; taxa mais alta ⇒ menor latência de viagem. As *proporções* são o que importa — manter a escada 1×/2×/4×/8× mesmo que escolhamos uma base mais alta que 10 para o ritmo moderno.)

- **Tubo de fluido:** taxa base **10 mB/t** — combinar o BC exatamente. v1 entrega o **tubo base (10 mB/t)** mais um **tier Dourado (8× = 80 mB/t)**; os tiers médios (Pedra 2× = 20, Ferro 4× = 40) são fast-follow. Todos os tiers mantêm as proporções 1×/2×/4×/8×. Transporte passivo (base/dourado/void) é **gratuito** — sem RF; apenas extração ativa custa energia (abaixo).
- **Tanque:** **16.000 mB (16 baldes)** — exatamente o `TileTank` do BC. Portar o comportamento de **empilhamento vertical e mesclagem** (tanques empilhados formam um único reservatório lógico, enchem de baixo para cima); é barato, amado e é o tanque "escala é a feature" feito como blocos ladrilháveis em vez de um multibloco.
- **Bomba:** `TilePump` do BC = **100 RF por bloco fonte drenado**, buffer interno de 16.000 mB, bombeia 1000 mB/operação em um ciclo de 16 ticks (~62,5 mB/t), empurra até 400 mB/t na rede. Ancorar aqui; se acopla à linha de motores — sem bombeamento gratuito.
- **Tubo extrator:** o tubo de madeira do BC liga extração ao poder do motor (≈`5 × base` RF por mB/t de pull). Espelhar o gate de energia do **Item Extractor Pipe** existente em vez de copiar a matemática exata de RF.
- **Refinaria (depois, cadeia de combustível):** para contexto — tanques BC de 4000 mB, ~3 óleo : 1 combustível; revisitar no brief da cadeia de combustível.

## Design sketch

Novo domínio `fluid`, paralelo ao `pipe`, registrado como `DomainBootstrap` (`LogisticsFluid implements DomainBootstrap`, listado em `META-INF/services/com.logistics.core.bootstrap.DomainBootstrap`).

```text
common/src/main/java/com/logistics/fluid/
├── LogisticsFluid.java            # DomainBootstrap: registradores BLOCK / ITEM / ENTITY / MENU
├── pipe/                          # tubo de transporte de fluido (veja nota abaixo)
├── tank/
│   ├── TankBlock.java             # extends BaseEntityBlock; mesclagem vertical de empilhamento; I/O plano todos os lados
│   └── TankBlockEntity.java       # extends BaseBlockEntity implements HasFluidStorage
│                                  #   envolve um FluidTankComponent (16k mB); saveLogisticsData/loadLogisticsData
└── pump/PumpBlock(+Entity).java   # fonte do mundo → armazenamento de fluido adjacente; 100 RF/bloco fonte (último PR)
common/src/client/java/com/logistics/fluid/   # renderização de preenchimento do tanque, renderização de fluido do tubo
```

**Tubos de fluido são standalone — fluido nunca entra no grafo logístico (decidido).** Tubos de fluido rodam seu próprio **transporte simples de pressão/equalização** (estilo tubo impermeável BuildCraft): fluido flui para vizinhos com menor preenchimento / sumidouro, limitado por tick pela taxa do tubo. Eles *não* são modelados como módulos no `Pipe`/`NetworkGraph` de item, e não há **refatoração planejada de rede de fluido**. Manter o *flowRate-como-cap-rígido* do BC como contrato de throughput; não precisamos copiar a maquinaria completa de seção/TTL/modo-entrada-saída do BC a menos que um simples equalizador se mostre insuficiente.

**Como "logística" de fluido acontece depois — fazer bridge de fluidos para itens, não ensinar a rede sobre fluidos.** Quando distribuição de fluido em nível de rede for desejada, a resposta é uma **máquina de empacotamento** que enche/esvazia containers (o **Fluid Transposer** / uma máquina de enlatamento) — convertendo fluido ↔ um *item* de container preenchido. A rede logística de itens existente então provê / requisita / roteia esses itens exatamente como qualquer outro item. **A rede logística permanece baseada em itens para sempre.** Fluidos recebem transporte local (tubos) + uma bridge (o transposer); eles nunca se tornam um primitivo de rede. Isso torna os tubos de fluido uma feature autocontida e delimitada.

**Set de tubos de fluido — roster BuildCraft → identidade Logistics.** O BC entregou ~11 variantes de tubo impermeável; por *"features ganham seu lugar,"* colapsar para um set enxuto mapeado na identidade existente de tubo de item do Logistics (movedores Stone/Copper, Extractor, Filter, Void, velocidade Golden):

| Necessidade | Origem BC | Tubo de fluido Logistics | Taxa | Entregar quando |
|---|---|---|---|---|
| Transporte base | Pedregulho / Pedra | Stone (ou Copper) Fluid Pipe | 10 mB/t | **fundação** |
| Throughput máximo | Ouro (8×) | Golden Fluid Pipe | 80 mB/t | **fundação** |
| Puxar de tanques/máquinas | Madeira | Fluid Extractor Pipe (com gate de energia; espelha Item Extractor Pipe) | 10 mB/t | **fundação** |
| Destruir fluido | Void | Void Fluid Pipe | — | **fundação** (trivial) |
| Throughput médio | Pedra 2× / Ferro 4× | um ou dois tiers médios | 20 / 40 mB/t | fast-follow |
| Sortear por tipo de fluido | Diamante | Fluid Filter Pipe | — | depois (apenas se houver demanda) |
| Saída direcional forçada | Ferro | — provavelmente pular (usar colocação / wrench) | — | — |

**Modelo de energia (decidido):** tubos de transporte passivo (base / dourado / void) movem fluido **gratuitamente**, como os tubos impermeáveis passivos do BC. Apenas **extração ativa** custa RF — o **tubo extrator** (espelhar o gate de energia do Item Extractor Pipe) e a **bomba** (100 RF/bloco fonte). Isso mantém o acoplamento de energia onde importa e permanece consistente com a direção de gate de energia de tubo de item (#464/#465/#469) sem taxar cada gota movida.

Note que o tubo de fluido é um **transporte de fluxo contínuo novo**, diferente dos tubos de item (discrete `TravelingItem`s) — ele compartilha o *domínio e estilo de renderização* dos tubos de item, não seu modelo de movimento. O nível de fluido de tanque/tubo é desenhado por um **renderer dinâmico de block-entity** (o sprite de fluido real na altura de preenchimento), não estágios de block-state — block-state não pode carregar cor/nível de fluido arbitrários.

**Conectar uma máquina para I/O de fluido:**
1. BE implementa `HasFluidStorage`, retorna seu `FluidTankComponent` (ou um wrapper por lado) de `fluidStorage(side)`.
2. Fabric: nada extra — o fallback SIDED de `FluidStorageAccess` já expõe qualquer BE `HasFluidStorage`.
3. NeoForge: habilitar o template `registerFluids(event, TYPE)` comentado em `NeoForgeCapabilityRegistration` e chamá-lo para o tipo de BE.

## Scope & non-goals

- **In:** o set de tubo de fluido de fundação (**base 10 + dourado 80 + extrator + void**), o tanque (16k mB, empilhamento vertical e mesclagem, renderização dinâmica), o contrato de I/O de fluido para máquinas, interop manual com balde, e a **Bomba** como o consumidor validador (último PR da feature).
- **Fast-follow (esta feature cresce para incluir):** os tiers médios de tubo (Pedra 20 / Ferro 40), um tubo de filtro de fluido — adicionados conforme a demanda mostra, não bloqueados na v1.
- **Out (features separadas):** motores de Combustão/Magnético, Magma Crucible, o **Fluid Transposer / máquina de empacotamento** (a bridge fluido↔item — seu próprio brief), a cadeia de combustível de óleo/biofuel, tanques em massa multibloco verdadeiros (Railcraft, Fase 3 — nosso tanque é *ladrilhável*, não um esquema).
- **Out (decidido, não meramente adiado):** qualquer modelo que coloque fluidos *na rede logística*. A distribuição de fluidos por uma base é entregue depois pela máquina de empacotamento + a rede de **item** existente — não há primitivo de rede de fluido, nunca.
- **Out:** inventar um registro de fluido ou tipos de fluido próprios — usar `Fluid`s vanilla (água/lava) e quaisquer combustíveis que a feature da cadeia de combustíveis defina.

## Decisions

Todas as questões bloqueadoras de início estão resolvidas — este brief está pronto para construir:

- **Arquitetura** — tubos de fluido são **standalone para sempre** (equalização simples, nunca no grafo logístico). Distribuição de fluido em nível de rede vem depois via uma **máquina de empacotamento** fluido↔item (Fluid Transposer); a rede logística permanece baseada em itens permanentemente.
- **Taxa de fluxo base** — **10 mB/t**, combinando o BuildCraft exatamente. Tiers mantêm as proporções 1×/2×/4×/8× (10 / 20 / 40 / 80).
- **Set de tubo de fundação** — **base (10) + dourado (80) + extrator + void**. Tiers médios (20/40) e um tubo de filtro são fast-follow.
- **Modelo de energia** — transporte passivo (base/dourado/void) é **gratuito**; apenas o **tubo extrator** e a **bomba** consomem RF. Consistente com a direção de gate de energia de tubo de item sem taxar cada gota.
- **Modelo de fluxo** — **equalizador simples** (fluxo para menor preenchimento/sumidouro, divisão proporcional ao espaço, limitado por taxa do tubo); pular a maquinaria de seção/delay-de-viagem do BC a menos que um protótipo prove necessário. Manter o **rate-como-cap-rígido** do BC e **um-fluido-por-linha, sem mistura**.
- **Tanque** — **16.000 mB**, I/O plano em todos os lados, **empilhamento vertical e mesclagem**, desenhado por um **BER dinâmico** (não estágios de block-state).
- **Bomba** — **parte desta feature**, construída como o **último PR** (é a prova de ponta a ponta da cadeia), ancorada no 100 RF/bloco fonte do BC.

> As escolhas restantes são detalhes de *implementação*, não bloqueadores: texturas/receitas exatas, as especificidades de ordem-de-tick do equalizador, e se os tiers médios chegam no mesmo lançamento ou no próximo.

## Done when

- Um tubo base (10 mB/t) e um tubo dourado (80 mB/t) carregam água/lava entre um tanque e um inventário de fluido vanilla/de outro mod em **ambos os loaders**, passivamente (sem RF), um fluido por linha.
- O tubo extrator puxa fluido de um tanque/máquina adjacente, consumindo RF; sem energia ele para.
- Um tanque armazena 16k mB através de save/load, enche/drena de um balde, **empilha verticalmente em um tanque lógico único** e renderiza seu fluido na altura de preenchimento.
- Uma BE de máquina expondo `HasFluidStorage` é legível/gravável pelo tubo de fluido em ambos os loaders (NeoForge `registerFluids` habilitado).
- A Bomba move uma fonte de fluido do mundo para um tanque/tubo adjacente, consumindo 100 RF/bloco fonte.

## References

- Roadmap: [`../roadmap.md`](../roadmap.md) → Fase 1 → 🔑 Fundação de Fluidos
- Detalhamentos: [`../mods/buildcraft.md`](../mods/buildcraft.md) (tubos de Fluido, Bomba, Óleo/Refinaria), [`../mods/thermal-expansion.md`](../mods/thermal-expansion.md) (Magma Crucible, Fluid Transposer)
- Precedente de código (lado de item, espelhar): `core/lib/storage/IItemStorage`, `core/lib/block/capability/HasItemStorage`, `core/lib/storage/ItemStorageLookup`, `fabric/.../capability/ItemStorageAccess`, `neoforge/.../NeoForgeCapabilityRegistration`
- Camada de fluido já construída: `core/lib/fluids/*`, `core/lib/block/capability/HasFluidStorage`, `fabric/.../fluids/*`, `neoforge/.../fluids/*`
- Âncora de equilíbrio BuildCraft (`../buildcraft`, v7.1.27 / MC 1.7.10): `common/buildcraft/transport/PipeTransportFluids.java` (taxa base 10 mB/t + multiplicadores por tubo; seções de 250 mB), `common/buildcraft/transport/pipes/PipeFluids*.java` (o roster de tubo impermeável), `common/buildcraft/factory/TileTank.java` (16.000 mB, mesclagem por empilhamento), `common/buildcraft/factory/TilePump.java` (100 RF/bloco fonte, ciclo de 16 ticks), `common/buildcraft/factory/TileRefinery.java` (cadeia de combustível, depois)
