# Tiers de Progressão

A **escada canônica de tiers** do mod — um vocabulário ordenado de materiais ancorados no vanilla que *toda* linha com tiers usa, para que "o tier ouro" signifique a mesma posição de progressão seja para um cabo, uma engrenagem ou uma bateria.

Isso expande dois princípios já em [`principles.md`](principles.md): **identidade baseada em material** (cada tier se lê do seu material vanilla à primeira vista) e **respeitar os tiers de progressão modernos** (mapear para a escada vanilla, não metais legados). Antes deste doc, as linhas divergiam — engrenagens rodavam uma escada de 9 passos (incl. `estanho`), cabos rodavam cobre/ouro/ender, cores/valves rodavam cobre/bronze. Este é a espinha compartilhada.

## A regra

- Há **uma escada canônica** (abaixo). Toda linha com tiers escolhe um **subconjunto dela, em ordem**, usando estes materiais e nomes.
- Uma linha usa os tiers de **flavor apropriado** — cabos usam condutores, engrenagens usam metais estruturais — mas nunca inventa uma ordenação paralela ou nomes fora da escada.
- **Nem toda linha precisa de muitos tiers.** Dois é suficiente; o ponto é consistência, não maximalismo.

## Fases de progressão (pontos de unlock)

Materiais mapeiam para cinco fases do jogo. A ordenação é pelo **tema/tier pretendido**, não estritamente pela dificuldade de obtenção no vanilla (ex.: pérolas do ender são obteníveis no overworld mas são tratadas como o tier "End / dimensional"; fragmentos de eco não requerem o End mas são o tier mais profundo e definitivo).

| Fase | Gate de unlock | Tiers principais | Feel |
|---|---|---|---|
| **Early game** | mineração de superfície / overworld raso | Cobre, Ferro *(Madeira/Pedra pré-tier)* | primeiras máquinas, mecânico, RF básico |
| **Mid game** | mineração profunda · fundição de ligas · geodes | Bronze, Ouro, Diamante, Ametista | rampa tech, processamento de minérios, ressonância/precisão |
| **Nether** | portal do Nether | Netherita *(acentos de Blaze/Quartzo)* | calor, craft endgame durável |
| **End** | acesso dimensional | Ender | logística sem fio / dimensional |
| **Deep dark** | cidade antiga | Fragmento de Eco | definitivo, sculk/dados |

## A escada canônica de materiais

Cada rank tem **dois rótulos canônicos**: o **material** (identidade visual — o que uma coisa é feita / parece) e o **nome de tier** (o adjetivo — o nome durável para o *conceito* do tier, e uma opção para nomes de itens). A **função** é o flavor fixo que esse rótulo carrega em toda linha — escolha o material/tier quando sua função se encaixa na linha.

| Rank | Material | Nome de tier | Função (durável) | Fase |
|---|---|---|---|---|
| 0 | Madeira / Pedra | **Crude** | improvisado, pré-energia | early |
| 1 | **Cobre** | **Basic** | entry-level, cavalo de batalha barato | early |
| 2 | **Ferro** | **Sturdy** | estrutural, resistente a carga | early |
| 3 | **Bronze** | **Industrial** | liga de máquina inicial | mid |
| 4 | **Ouro** | **Conductive** | throughput de energia / sinal | mid |
| 5 | **Diamante** | **Precision** | precisão, alta qualidade | mid |
| 6 | **Ametista** | **Resonant** | ressonância / buffer / carga-sem-fio | mid |
| 7 | **Netherita** | **Infernal** | calor, durabilidade | nether |
| 8 | **Ender** | **Dimensional** | teleporte / cross-dimension / transporte sem fio | end |
| 9 | **Fragmento de Eco** | **Deep** | sculk / sensoriamento / dados · definitivo | deep dark |

## Nomenclatura

Nomes de exibição de itens podem derivar de **qualquer um** dos rótulos. Padrão: usar o nome do **material** (Cabo de Cobre, Engrenagem de Bronze — honra a identidade do material); usar o **adjetivo de tier** quando lê melhor ou o material fica estranho (um tier ametista "Ressonante", um tier ender "Dimensional"). Manter consistência dentro de uma linha.

## Cobertura — onde cada material se encaixa

A espinha lista apenas tiers com **peso de progressão**. O sistema é *total*: todo outro material — vanilla ou nosso — cai em exatamente um destes, então nada é ambíguo quanto a "que tier é isso?"

- **Matéria-prima de liga (sem rank):** **Estanho**, **Nickel** — existem apenas para craftar ligas (Bronze, Invar); nunca um tier por conta própria. (O nickel subproduto do Macerador alimenta o Invar — veja [`features/0105-alloy-smelter.md`](features/0105-alloy-smelter.md).)
- **Nossas ligas (ocupam um rank):** **Bronze** = rank 3 (Industrial). **Invar** = uma liga estrutural/de precisão por volta do rank 3–4 (entre Industrial e Conductive); seu papel é frames de máquinas / componentes de precisão, então participa como *material componente*, não um tier visual separado. Novas ligas assumem o rank de seu papel, mantêm seu próprio nome e respeitam a ordenação.
- **Acentos de função (sem rank):** carregam uma *função*, não um passo de progressão — use-os pelo sistema que evocam, não como tiers: **Redstone** → sinal/lógica (reservar para gates/circuitos; *não* cabos de energia), **Lápis** → encantamento, **Esmeralda** → comércio, **Blaze / Quartzo / Pedra Brilhante** → componentes do Nether, **Obsidiana** → contenção/explosão.
- **Fora do escopo:** materiais puramente decorativos/de mundo não definem tier algum.

> Regra geral: um material é um **rank da espinha** (gate de progressão), uma **liga** (ocupa um rank), uma **matéria-prima** (faz uma liga), um **acento** (uma função, não um passo), ou **fora do escopo**.

## Mapeamento por linha

Cada linha escolhe seu subconjunto em ordem canônica. *(Subconjuntos ilustrativos — refinar por linha conforme construída.)* Por nome de tier, a linha de cabos lê **Basic → Conductive → Resonant → Dimensional**.

| Linha | Flavor | Subconjunto canônico |
|---|---|---|
| **Cabos** | condutividade | Cobre · Ouro · **Ametista** · Ender |
| **Engrenagens** | mecânico | Madeira · Pedra · Cobre · Ferro · Bronze · Ouro · Diamante · Netherita *(remover Estanho)* |
| **Baterias** ([0107](features/0107-tiered-batteries.md)) | armazenamento | Cobre · Ouro · Ender |
| **Cores / Valves** | eletrônica | Cobre · Bronze · Ouro · Ametista |
| **Frames de máquinas** (futuro) | estrutural | Ferro · Bronze · Diamante · Netherita |
| **Chassis MkI–V** | contagem de slots | *exceção intencional — numérico, não um tier de material* |

## Plano de retrofit (implementação)

Per a decisão do mantenedor, **linhas existentes também se conformam** (não só trabalho novo). Estas são tarefas de implementação para PRs posteriores nas branches `mc/*` — este doc registra a decisão; as mudanças de código são separadas:

- **Cabos** — adicionar um tier **Ametista** → `Cobre 30 / Ouro 60 / Ametista 120 / Ender 240` RF/t (mantém a escada ×2; Ametista assume a taxa antiga do Ender e o Ender sobe para headroom no late-game — veja [`features/0107-tiered-batteries.md`](features/0107-tiered-batteries.md)). Números ajustáveis contra a curva de RF. Toca `power/cable/CableTier` + um bloco/modelo/receita `amethyst_cable`.
- **Engrenagens** — remover `tin_gear`; reordenar para o canônico Madeira · Pedra · Cobre · Ferro · Bronze · Ouro · Diamante · Netherita. *Migração:* remover `tin_gear` é breaking para mundos existentes — aceitável pré-1.0, ou remapear `tin_gear → bronze_gear` via data-fixer/receita.
- **Cores / Valves** — atualmente Cobre · Bronze; estender para Cobre · Bronze · Ouro · Ametista conforme o sistema de eletrônica cresce (se acopla ao trabalho adiado de comportamento programável — [`rfcs/0001-programmable-behavior.md`](rfcs/0001-programmable-behavior.md)). Sem mudança forçada agora.

## References

- [`principles.md`](principles.md) — identidade baseada em material, "respeitar tiers de progressão modernos" (este doc é a realização concreta)
- [`vision.md`](vision.md) — identidade baseada em material / princípios de progressão em camadas
- Aplicado em: [`features/0107-tiered-batteries.md`](features/0107-tiered-batteries.md) (cabos/baterias), [`features/0105-alloy-smelter.md`](features/0105-alloy-smelter.md) (ranks Bronze/Invar)
