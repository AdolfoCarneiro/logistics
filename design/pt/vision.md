# Visão

## O objetivo

Recriar a experiência de jogo de um **modpack de tech clássico da era 1.7.10** em um cliente moderno do Minecraft — com um único mod coeso (ou família pequena de mods) que *pareça* clássico, em vez de simplesmente transplantar conteúdo antigo para versões novas.

Pense como um **paralelo ao Create, mas clássico**: onde o Create abraça engrenagens cinéticas e uma estética própria, o Logistics segue a linhagem BuildCraft/Thermal/Forestry — tubos com itens visíveis se movendo, motores que superaquecem, máquinas RF, duplicação de minérios, abelhas e árvores, e trilhos que ligam uma base.

> *Nota pessoal: isso surgiu de um pack 1.7.10 que eu rodava. O alvo é o gênero que aquele pack representava, não o conteúdo exato de nenhum pack específico.*

## Material de origem

A identidade tech daquela era veio de um punhado de mods, **todos hoje abandonados**. A jogabilidade deles é o alvo:

| Mod de origem | O que contribuiu | Status no Logistics |
|---|---|---|
| **BuildCraft** | Tubos, motores, a quarry, gates/automação | Amplamente coberto (tubos, motores, laser quarry) |
| **Logistics Pipes** | Logística de rede com request/provider/supplier — *a* cola que unia uma base | **Feito** (rede de tubos em 3 camadas, chassis + módulos) |
| **Thermal Expansion** | Máquinas RF, processamento de minérios (pulverizador/pós), células de energia, fluidos | Parcialmente coberto (macerador, forno, pós, cabos) |
| **Forestry** | Abelhas, árvores, borboletas, fazendas/multifazendas, tubos eletrônicos, a bancada | Não iniciado |
| **Railcraft** | Trilhos, minecarts avançados, tanques, sinais, fornos de coque, caldeiras | Não iniciado |

Veja o detalhamento de cada um em [`mods/`](mods/).

### Mods adjacentes de packs (contexto, maioria fora do escopo)

Packs desse estilo também dependiam de mods de armazenamento e qualidade de vida. A maioria tem equivalentes modernos e **não** é responsabilidade do Logistics:

- **Armazenamento** — Iron Chests, Iron Tanks, Storage Drawers existem em forma moderna; **Ender Storage** foi abandonado mas tem substitutos modernos. Fora do escopo (Logistics fornece os *tubos*, não as *caixas*).
- **Mundo/estética** — Biomes O' Plenty, Binnie's Mods (extensão do Forestry). Fora do escopo, embora genética estilo Forestry possa se sobrepor.
- **Magia** — Soul Shards. Fora do escopo.
- **Ferramentas/QoL** — NEI, Waila, Schematica, Chisel, Carpenter's Blocks. Fora do escopo (equivalentes modernos existem; integramos com JEI/Jade quando ajuda).

## Princípios de design

Estende os princípios já declarados no [`README.md`](../README.md) do projeto:

- **Identidade baseada em materiais** — cada bloco é legível de relance pelos seus materiais vanilla (pedra, cobre, ouro, diamante, obsidiana…).
- **Progressão em camadas** — o modelo de tubos em três camadas (Mecânico → Inteligente → Rede), e de forma mais ampla: barato-e-simples no início, poderoso-e-abstrato no final.
- **Visuais autênticos** — itens viajam visivelmente pelos tubos; motores aquecem visivelmente; máquinas têm estado que pode ser visto.
- **Interoperabilidade com mods** — fala as APIs de energia/item/fluido da plataforma para que inventários e máquinas de qualquer mod participem.
- **Ergonomia clássica** — colocação simples, conexões visíveis, aprendível sem wiki.

Mais dois que guiam *este* esforço especificamente:

- **Sensação clássica, vanilla moderno.** Quando as premissas do mod antigo colidem com o Minecraft moderno (cobre agora existe; árvores soltam resina, não "seiva"; o bloco Crafter existe), **modernizamos para encaixar no vanilla** em vez de reproduzir mecânicas ultrapassadas. Veja [`principles.md`](principles.md).
- **Equilíbrio é o produto entregue.** O estado final deve parecer uma progressão coesa e equilibrada da era 1.7.10 — não uma pilha de features importadas. Cada decisão de portar/modernizar é tomada com a curva toda em mente.

## Como é "completo"

Um jogador pode instalar o Logistics (ou sua família de módulos) em um cliente moderno e jogar uma progressão que espelha reconhecivelmente o arco tech clássico da era 1.7.10:

1. Tubos mecânicos iniciais + motores de redstone/stirling.
2. Processamento de minérios e máquinas RF (a camada Thermal).
3. Automação de rede logística ligando armazenamento e crafting.
4. Automação biológica estilo Forestry (abelhas/árvores/fazendas) para variedade no endgame.
5. Transporte estilo Railcraft e processamento em massa para logística entre bases.

…tudo enquanto permite que jogadores instalem **apenas as partes que quiserem** (veja [`architecture.md`](architecture.md)).

## Não-objetivos

- Não é um clone 1:1 de receitas ou números de nenhum mod de origem específico.
- Não é um port de cada bloco — features precisam ganhar seu lugar contra o equilíbrio e o teste da "sensação clássica".
- Geralmente **não** são estruturas multiblocos monolíticas (veja a posição sobre multiblocos em [`principles.md`](principles.md)).
- Não são blocos de armazenamento, estética de geração de mundo ou magia — esses são trabalho de outros mods.
