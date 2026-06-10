# RFC 0003: Multiblocos & a Cadeia de Vapor do Railcraft

> **Status:** 🟡 Open — precisa de Discussion antes do agendamento da Fase 3 · **Scope:** Fase 3 (Transporte) · **Decides:** mantenedor + comunidade
> **Affects:** [`../mods/railcraft.md`](../mods/railcraft.md) § Vapor/aço/massa (Forno de Coque, Alto-Forno, Caldeira, Tanque) + o TODO de vapor/energia · **Blocks:** agendar a cadeia de vapor/aço da Fase 3 — não trilhos/carts/sinais da Fase 3

A maior tensão de design única no módulo Railcraft: seus blocos assinatura eram **multiblocos**, e [`../principles.md`](../principles.md) tende a **evitar multiblocos monolíticos**. Este RFC decide a forma de cada um, além de se vapor é um nível real de energia.

## Contexto

Os blocos de processamento/energia do Railcraft eram estruturas: **Forno de Coque** (carvão → coque + óleo de creosoto), **Alto-Forno** (ferro + coque → aço), **Caldeira a Vapor** (combustível + água → vapor, até 36 blocos), e **Tanque de Ferro/Aço** (armazenamento de fluido em massa). A posição dos princípios: preferir um **bloco único + marcadores/ladrilháveis opcionais**; permitir um multibloco somente quando *a escala em si é a feature*, e mesmo assim preferir **modular/ladrilhável** a um esquema rígido obrigatório.

[`../mods/railcraft.md`](../mods/railcraft.md) já inclina para bloco único no Alto-Forno ("bloco único preferido à estrutura de 34 blocos") e sinaliza o **Tanque** de massa como o único lugar onde "escala é a feature" poderia justificar uma exceção. O escopo de energia a vapor também está em aberto: vapor é um nível real em `power` (caldeira → turbina → RF, unificado na linha de motores) ou simplificado?

Esta é a **Fase 3 / pós-1.0**, então *não* bloqueia 1.0 — mas define o feel do módulo de transporte inteiro e deve ser decidido antes de a Fase 3 ser agendada.

## A decisão a tomar

**Para cada um de {Forno de Coque, Alto-Forno (Aço), Caldeira a Vapor, Tanque de Massa}: bloco único, ladrilhável/modular ou multibloco real? E: vapor é um nível real de energia, ou simplificado?**

## Disposição recomendada (a proposta a debater)

| Bloco | Forma clássica | Forma proposta | Justificativa |
|---|---|---|---|
| **Forno de Coque** | Multibloco | **Bloco único** (ou ladrilhável) | Produz coque + creosoto; escala não é o ponto — manter a linha anti-multibloco. |
| **Alto-Forno → Aço** | Multibloco de 34 blocos | **Máquina de bloco único** | Detalhamento já prefere isso; aço é o material chave do módulo e não deve precisar de um esquema. Combina com o padrão do Alloy Smelter. |
| **Caldeira a Vapor** | Multibloco de até 36 blocos | **Nível da linha de motores de bloco único *ou* cortado** | Ligado à decisão de vapor abaixo. Se vapor fica, um nível de caldeira de bloco único; se não, cortar. |
| **Tanque de Massa** | Multibloco | **Blocos de tanque ladrilháveis** (auto-mesclam em um tanque lógico único) | O único caso genuíno de "escala é a feature" — mas ladrilhável, não um esquema rígido. A exceção válida. |

**Escopo de energia a vapor:** duas sub-opções —
- **(i) Vapor é um nível real** — caldeira → turbina → RF, unificado na linha de motores (consistente com a thread de "unificar tudo em uma linha de motores" em [`../mods/buildcraft.md`](../mods/buildcraft.md)/[`../mods/thermal-expansion.md`](../mods/thermal-expansion.md)).
- **(ii) Simplificado** — entregar coque/creosoto/aço e trilhos com madeira tratada **sem** um nível de energia a vapor; carts rodam na energia RF/motores existente.

**Inclinação:** manter bloco único para Forno de Coque + Alto-Forno; permitir Tanques de Massa **ladrilháveis** como a única exceção de escala-é-a-feature; tratar vapor como **(ii) simplificado para o v1 do módulo de transporte**, revisitando um nível de motor a vapor somente se ganhar seu lugar.

## Sub-questões ainda em aberto

- O "tanque ladrilhável" precisa de um **bloco controlador/válvula**, ou blocos de tanque adjacentes auto-formam um multibloco lógico com conteúdo compartilhado?
- **Acoplamento de progressão:** trilhos/aço *requerem* a cadeia coque→creosoto→madeira-tratada, ou o nível ferroviário pode ser alcançado sem a cadeia de vapor? (Afeta se cortar vapor deixa conteúdo órfão.)
- Se vapor for cortado (ii), há conteúdo que *só* fazia sentido com uma caldeira (ex.: bloco de turbina)? Confirmar que nada fica órfão.
- **World Anchor / chunkloading** é um TBD sensível *separado* (performance do servidor) — notar aqui mas resolver em sua própria Discussion, não nesta.

## Como decidiremos

Discussion, pesando **tédio vs. satisfação da escala** por bloco, contra a posição sobre multiblocos. Registrar o resultado por bloco de volta nas linhas de `railcraft.md` (mudar Modernize/TBD → a forma concreta escolhida). Resolver antes do agendamento da Fase 3.

## Referências

- Detalhamento: [`../mods/railcraft.md`](../mods/railcraft.md) § Vapor/aço/massa + os TODOs de multibloco e vapor/energia
- Princípios: [`../principles.md`](../principles.md) § A posição sobre multiblocos
- Roadmap: [`../roadmap.md`](../roadmap.md) → Fase 3 → Cadeia de vapor & aço (nota os TBDs)
- Relacionado: thread de unificação da linha de motores em [`../mods/buildcraft.md`](../mods/buildcraft.md) / [`../mods/thermal-expansion.md`](../mods/thermal-expansion.md); Aço combina com [`../features/0105-alloy-smelter.md`](../features/0105-alloy-smelter.md)
