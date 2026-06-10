# RFC 0001: Comportamento Programável (Gates + Circuitos)

> **Status:** 🟡 Open — **adiado pós-1.0** (decisão do mantenedor, jun 2026): explicitamente **não é um item de 1.0**; revisitar quando o Forestry precisar de circuit boards (Fase 2) ou depois · **Scope:** Fase 2+ · **Decides:** mantenedor + sinal da comunidade
> **Affects:** [`../mods/buildcraft.md`](../mods/buildcraft.md) (Gates, Pipe wiring, Autarchic gate), [`../mods/forestry.md`](../mods/forestry.md) (Circuit boards + Soldering), [`../mods/thermal-expansion.md`](../mods/thermal-expansion.md) (augments programáveis) · **Blocks:** agendar as linhas de gate do BC e as linhas de circuit-board do Forestry — **não** as máquinas/fazendas, e **não** os upgrades de modificador de máquina da Fase 1 ([`../features/0106-machine-upgrades.md`](../features/0106-machine-upgrades.md))

A maior decisão de design em aberto abrangendo os mods de origem. Três deles entregaram um sistema de "fazer este bloco fazer algo condicional"; o roadmap sugere unificá-los. Este RFC enquadra a decisão.

## Contexto

Três linhagens de "programar o comportamento de um bloco":

- **Gates do BuildCraft** — lógica trigger → action encaixada em tubos/máquinas (níveis básico/ferro/ouro/diamante), com fiação colorida de tubos para carregar sinais. *Lógica.*
- **Circuit boards + soldering do Forestry** — programar comportamento de máquina/fazenda arranjando **tubos eletrônicos** em uma placa. *Lógica / configuração.*
- **Augments do Thermal Expansion** — **modificadores** de máquina inseridos em slots (velocidade/eficiência/secundário/saída-automática). *Modificadores, não lógica.*

**Distinção importante que este RFC traça:** o eixo de *modificadores* (augments TE) já está sendo tratado como [upgrades de máquinas](../features/0106-machine-upgrades.md) (`0106`). Isso **não** é o que está contestado aqui. Este RFC é especificamente sobre **lógica programável** — gates e circuitos — ou seja, "quando a condição X é verdadeira, faça a ação Y." O trabalho de augment pode *compartilhar um vocabulário de itens* (o Logistics já tem logic chips, cores e valves que espelham os tubos eletrônicos do Forestry), mas o sistema de lógica é uma questão de design separada e muito maior.

A tensão com [`../principles.md`](../principles.md): lógica programável é de alto valor para power users mas arrisca violar **"aprendível sem wiki"** e é um grande esforço de design + implementação transversal. O Minecraft moderno também já tem redstone + comparadores, que cobrem muitos dos casos simples que gates faziam.

## A decisão a tomar

**Construímos um subsistema de lógica programável unificado, apoiamo-nos no redstone vanilla com apenas hooks específicos por feature, ou adiamos lógica programável inteiramente para v1?** E se unificado — quão profundo (listas trigger→action vs. um circuito visual)?

## Opções

### Opção A — Sistema unificado de comportamento programável
Uma camada de lógica plugável em tubos, máquinas e fazendas: ler estado (nível de inventário, energia, demanda de rede, status de crafting) → tomar ações (toggle, emitir redstone, controlar roteamento). Craftado a partir dos componentes existentes tipo tubo-eletrônico (cores/valves/logic chips). Possivelmente um editor visual/de placa (estilo Forestry) ou regras compactas trigger→action (estilo gate BC).
- **Prós:** a automação tech clássica mais profunda e fiel; um sistema coeso em vez de três; reutiliza componentes já semeados.
- **Contras:** enorme esforço de design + UX + implementação; maior risco de "precisa de wiki"; abrange expectativas de três mods; fácil de over-scope.

### Opção B — Hooks específicos voltados ao vanilla *(inclinação para v1)*
Sem sistema geral de programação. Apoiar-se em redstone/comparadores vanilla, e adicionar apenas **configuração estreita por feature**: ex.: tubo/máquina que emite redstone em uma condição, extrator que respeita sinal de redstone, enable/disable de fazenda. Reutilizar os UIs de config de módulo existentes.
- **Prós:** baixa complexidade; entrega incrementalmente; permanece "aprendível"; não bloqueia 1.0.
- **Contras:** menos profundidade para power users; "gates" como feature distinta vira efetivamente "skip / use redstone"; sem camada programável única.

### Opção C — Pular lógica programável para v1
Entregar máquinas/fazendas com comportamento fixo + hooks básicos de redstone; adiar todos os gates/circuitos pós-1.0.
- **Prós:** mais simples; escopo 1.0 mais claro.
- **Contras:** deixa uma mecânica clássica reconhecível ausente; adia em vez de decidir a unificação.

## Recomendação / inclinação

**B por agora; A é explicitamente pós-1.0 — adiado até o Forestry precisar (decidido, jun 2026).** Justificativa: 1.0 = núcleo de automação da Fase 1 completo ([`../roadmap.md`](../roadmap.md)); um sistema geral de programação *não* é obrigatório para isso e seria um grande risco de escopo. O mantenedor confirmou que o sistema unificado **não é um item de 1.0** — o gatilho natural para revisitá-lo são os **circuit boards** do Forestry (programar blocos Forestry, Fase 2), ou depois. Enquanto isso, hooks de redstone específicos (B) cobrem os casos comuns; C é o fallback se nem esses forem desejados. Quando revisitado, deixar a demanda da comunidade (poll) pesar a complexidade de A.

## Sub-questões ainda em aberto

- Que **estado** de máquina/rede deve ser legível (níveis, energia, demanda, crafting em andamento)?
- Que **ações** estão no escopo (redstone out, toggle, rotear/desviar)?
- Se A: placa visual vs. listas de regras trigger→action? Como evitar dependência de wiki?
- Quanto os **cores/valves/logic chips** existentes se tornam o vocabulário de crafting independente de qual opção vencer?
- O **Autarchic gate** (motor de redstone com auto-pulsação) simplesmente se integra à config de motor/extrator (já anotado como "Modernize" em `buildcraft.md`) independente deste RFC? *(Provavelmente sim — tratar separadamente.)*

## Como decidiremos

**Discussion** de Ideas/Polls (abrange três mods e é baseado em gosto). Pesar contra o orçamento de complexidade e o princípio "aprendível sem wiki". O resultado muda as linhas TBD afetadas nos detalhamentos para Port/Modernize/Skip e, se A/B, semeia um feature brief.

## Referências

- Nota do roadmap: [`../roadmap.md`](../roadmap.md) → Fase 1 → "Comportamento programável — explicitamente pós-1.0 (adiado)"
- Detalhamentos: [`../mods/buildcraft.md`](../mods/buildcraft.md) (linhas Gates/wiring + TODO), [`../mods/forestry.md`](../mods/forestry.md) (linha Circuit boards + TODO), [`../mods/thermal-expansion.md`](../mods/thermal-expansion.md) (Augments)
- Feature brief relacionado (eixo de modificadores, *não* este RFC): [`../features/0106-machine-upgrades.md`](../features/0106-machine-upgrades.md)
- Componentes já semeados: `core` cores / valves / logic chips
