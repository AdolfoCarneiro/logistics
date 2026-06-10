# Forestry

*A camada de "variedade no endgame". Para nossos propósitos as **fazendas** são o destaque — seguidas de perto pelas **máquinas de processamento e a energia** que as alimentam — depois árvores e eletrônica. **Abelhas estão adiadas** (o modelo de criação vanilla diverge muito do Forestry; veja abaixo) e borboletas são puladas. Base já semeada: apatita para fertilizante, e os componentes lógico-chip/core/válvula se assemelham à eletrônica de tubos eletrônicos. Esta é a Fase 2.*

**Era de origem:** 1.7.10–1.12.2 (Forestry).
**Módulo Logistics:** `logistics-forestry` (novo).
**Fase:** 2.

Veja [`../principles.md`](../principles.md) para a legenda da tabela. Forestry é grande; as decisões abaixo favorecem **os designs de bloco único mais antigos** e **evitar multiblocos pesados** (veja a posição sobre multiblocos).

## Fazendas (agricultura) — o destaque

| Feature | What it did (1.7.10–1.12.2) | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
| Fazenda (bloco único) | Plantio/colheita automatizada de um tipo de fazenda | Port | Alvo: **fazendas Forestry de bloco único mais antigas**, *não* a Multifarm multibloco posterior. Um bloco por fazenda + seleção de tipo; upgrades de alcance/velocidade | — | Fase 2 — fazendas |
| Multifarm (multibloco posterior) | Grande estrutura modular de fazenda gerenciada | Skip | Deliberadamente não portando a direção multibloco para a qual o Forestry migrou — manter fazendas como blocos únicos | ❌ | — |
| Pântano de turfa / turfa | Crescer turfa → combustível sólido | Port | Bom loop de combustível inicial; combina com fertilizante de apatita | — | Fase 2 — fazendas/combustível |
| Fertilizante / húmus / composto | Fertilizante base de apatita acelera crescimento | Modernize | **Apatita já implementada** — construir a cadeia de fertilizante sobre ela | 🚧 Planned | `core` / Apatita → fertilizante |

## Máquinas de processamento & energia

*A cadeia que alimenta as fazendas e transforma sua produção em combustível e componentes — segunda prioridade logo após as fazendas.*

| Feature | What it did | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
| Carpenter | Crafting assistido por líquido (circuitos, itens impregnados, etc.) | Modernize | Máquina chave, mas o original era **pouco intuitivo** — redesenhar em uma máquina de crafting mais clara mantendo seu papel | — | Fase 2 — Carpenter |
| Squeezer | Sementes/frutas/mel → líquidos (óleo de semente, suco) | Port | Alimenta biocombustível + Carpenter | — | Fase 2 — processamento |
| Fermenter | Mudas/biomassa + fertilizante → líquido de biomassa | Port | Passo 1 da cadeia de biocombustível | — | Fase 2 — biocombustível |
| Still | Biomassa → etanol/biocombustível | Port | Passo 2 da cadeia de biocombustível; um **combustível paralelo** para o motor de combustão (se acopla ao BuildCraft) | — | Fase 2 — biocombustível |
| Bottler / Enchimento de latas | Encher recipientes com fluidos | Port | Depende da camada de fluidos | — | Fase 2 — fluidos |
| Motores (turfa / biogás / biocombustível / elétrico) | Linha de motores própria do Forestry | Modernize | **Unificar com a linha de motores**: níveis de motor a turfa + biocombustível em vez de um sistema paralelo. Este é "a energia que vinha com as fazendas" | — | `power` / engines |
| Moistener | Fazer micelium/blocos cobertos de musgo | Modernize | Menor; baixa prioridade | — | — |

## Árvores (arboricultura)

| Feature | What it did | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
| Arboricultura | Cruzar espécies de árvores; mudas/pólen; enxertador; treealyzer | Port | Produz variedade de madeira + frutas; poderia começar com um conjunto de espécies mais simples antes de genética profunda | — | Fase 2 — árvores |
| Produtos de árvore (fruta/madeira/muda) | Madeiras especiais, frutas, látex | Modernize | Alinhar com tipos de madeira vanilla + **resina** (não "seiva") | — | Fase 2 — árvores |

## Eletrônica (tubos eletrônicos & circuitos)

| Feature | What it did | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
| Thermionic Fabricator | Vidro derretido + lingotes → **tubos eletrônicos** | Modernize | Como o Carpenter, o original era **pouco intuitivo** — redesenhar para clareza, idealmente **unificar com o Carpenter** em uma máquina de componentes mais acessível | — | Fase 2 — eletrônica |
| Tubos eletrônicos (cobre/estanho/bronze/ferro/ouro/diamante/brasa/apatita/lápis/ender/etc.) | Componentes tipados para programação de circuitos | Modernize | **Já semeado**: o Logistics tem *logic chips*, *cores* (13) e *valves* (13) que espelham este conjunto de tubos | 🚧 Planned | `core` / cores, valves, logic chips |
| Circuit boards + Soldering | Programar comportamento de máquina/fazenda via layouts de tubos | TBD | **Unificar** circuitos com augments estilo TE + gates do BC em um sistema de "comportamento programável" em todo o mod | — | Fase 1/2 — eletrônica (RFC) |

## Abelhas (apicultura) — adiado

> **Abelhas não são destaque da Fase 2 e podem ser puladas inteiramente para v1.** O vanilla moderno já cria abelhas alimentando-as com flores — um modelo que diverge muito do ciclo de vida de princesa/zangão/rainha do Forestry e sua genética Mendeliana. Reconciliar os dois (estender o vanilla? sistema paralelo completo? pular?) precisa de uma passagem de design deliberada antes de qualquer trabalho de abelhas ser agendado — marcar para Discussion de Ideas.

| Feature | What it did | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
| Apiário | Colmeia de bloco único; abelhas produzem favos | Modernize | **Vanilla já tem colmeia/ninho de abelhas** — estender seu comportamento em vez de adicionar um bloco paralelo | — | — |
| Ciclo de vida de abelhas (princesa/zangão/rainha) | Criar rainhas; descendentes; decay | TBD | Fundamentalmente diferente da criação de flores vanilla; o cerne da questão se abelhas acontecem ou não | — | — |
| Genética / mutações de abelhas | Traços Mendelianos + descoberta de espécies | TBD | Depende da decisão do ciclo de vida; se perseguido, simplificar a matriz de traços | — | — |
| Produtos de abelhas + Centrífuga | Favos → centrífuga → mel/cera/geleia | TBD | Acoplado às abelhas; adiado com elas | — | — |
| Frames / Alveary / ferramentas de abelha | Modificadores de saída; multibloco avançado; análise | TBD | Adiado; Alveary também conflitaria com a posição sobre multiblocos | — | — |

## Fora do escopo

| Feature | What it did | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
| Borboletas (lepidopterologia) | Criar borboletas; casulos; soro | Skip | Skip direto por ora — decorativo, baixa jogabilidade por complexidade | ❌ | — |
| Mochilas (lenhador/minerador/escavador/etc.) | Armazenamento categorizado portátil | Skip | Armazenamento é trabalho de outros mods | ❌ | — |
| Caixotes | Armazenamento compacto | Skip | Fora do escopo | ❌ | — |
| Correio / Cartas / Estações de Trade | Postal in-game + auto-trade | Skip | Nicho; fora do escopo | ❌ | — |

> TODO: confirmar o exato modelo de receita tubo-eletrônico → circuit-board, e se deve ser mesclado com augments TE + gates BC em um único subsistema de "comportamento programável" (provavelmente uma Discussion de Ideas — abrange três mods de origem). Veja [RFC 0001 — Comportamento Programável](../rfcs/0001-programmable-behavior.md).
> TODO: resolver a questão das abelhas (estender vanilla / sistema paralelo / pular para v1) antes de qualquer trabalho de abelhas da Fase 2 ser agendado. Veja [RFC 0002 — Abelhas Forestry](../rfcs/0002-forestry-bees.md).
