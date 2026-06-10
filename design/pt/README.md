# Logistics — Design & Roadmap

Esta pasta é o **cérebro de planejamento** do mod Logistics. Contém o "por quê" durável e "o que decidimos": a visão, os princípios de design, um detalhamento feature-a-feature dos mods clássicos que servem de referência, a arquitetura-alvo de módulos e o roadmap por fases.

**Não** é a documentação voltada ao usuário. A docs do jogador ficam na branch `docs` (o site Zensical publicado em <https://indemnity83.github.io/logistics/>). Esta pasta é para contribuidores e mantenedores, e pode ser opinionada e em construção.

## Modelo operacional

O trabalho é dividido em três ferramentas, cada uma fazendo o que faz melhor:

| Trabalho | Ferramenta | Por quê |
|---|---|---|
| **O "por quê" + decisões** (visão, detalhamentos por mod, arquitetura) | **Markdown, aqui em `design/`** | Narrativo, versionado, revisável em PRs. Raramente muda depois de decidido. |
| **Progresso ao vivo** (o que está sendo construído, status, datas) | **GitHub Project #4 "Logistics Roadmap"** | Filtrável, com datas, colunas de status, hierarquia épico→sub-issue. |
| **Feedback** (decisões contestadas, votos da comunidade) | **GitHub Discussions** (Ideas / Polls) | Aberto, reações como sinal de demanda. |

**Markdown primeiro, depois o quadro.** Elaboramos e debatemos as decisões aqui. Quando uma seção consolida, decompomos no Project: cada *linha de feature* em um detalhamento vira uma *sub-issue*, agrupada sob um *épico de área de mod*, arquivada sob um *milestone de fase*. O doc explica a decisão; a issue rastreia o trabalho. Não são duplicatas.

## O que tem aqui

| Arquivo | O que é |
|---|---|
| [`vision.md`](vision.md) | O objetivo: recriar a experiência tech clássica da era 1.7.10 no Minecraft moderno. Limites de escopo. |
| [`principles.md`](principles.md) | O framework de decisão (**Portar / Modernizar / Pular**), regras de modernização, filosofia de equilíbrio, e o template de tabela de decisão compartilhado. |
| [`architecture.md`](architecture.md) | Domínios atuais e a divisão **provisional** de módulos/jars. |
| [`progression-tiers.md`](progression-tiers.md) | A **escada canônica de tiers** (Cobre→Echo Shard), fases de unlock e a convenção "escolha um subconjunto em ordem" que toda linha em tiers segue. |
| [`roadmap.md`](roadmap.md) | O plano por fases (Fase 0–3), o que está feito e como mapeia para o Project #4. |
| [`mods/`](mods/) | Um detalhamento de features por mod de origem: BuildCraft, Logistics Pipes, Thermal Expansion, Forestry, Railcraft. |
| [`features/`](features/) | **Feature briefs** — linhas do roadmap consolidadas expandidas em specs prontas para construir (problema, requisitos, esboço de design baseado no código, questões em aberto). Um arquivo por feature. |
| [`rfcs/`](rfcs/) | **RFCs** — as decisões de design contestadas e não resolvidas (linhas TBD): a questão, opções com trade-offs, uma inclinação e como decidiremos. Escritas aqui, depois levadas para uma Discussion. |

## Como ler um detalhamento

Todo arquivo `mods/*.md` usa a mesma tabela de decisão. Veja [`principles.md`](principles.md) para o template completo e legenda. Resumindo: cada linha é uma feature, marcada com uma **Decisão** (Portar / Modernizar / Pular), um **Status** (✅ Feito / 🚧 Planejado / — Não iniciado / ❌ Não portará), e para onde ela **mapeia** no código ou no roadmap.

## Como contribuir com a direção

- **Propor ou debater uma decisão:** abra uma GitHub Discussion em **Ideas** (ou **Polls** para votação). Linke a seção relevante de `design/`.
- **Mudar uma decisão registrada:** abra um PR editando o arquivo `design/` relevante. O diff *é* o registro da mudança.
- **Pegar trabalho:** encontre a issue no Project #4; a coluna `Maps to` conecta o doc ao quadro.
