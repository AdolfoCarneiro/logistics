# Saídas Secundárias / Subprodutos do Macerador

> **Status:** 🚧 Planned — **design do mecanismo resolvido, pronto para construir** (mapa de conteúdo de subprodutos adiado para `0105`) · **Phase:** 1 — Automation core · **Module:** `logistics-automation` (domínio `core` — Macerador)
> **Source:** [`../mods/thermal-expansion.md`](../mods/thermal-expansion.md) (saída secundária do Pulverizer) · **Depends on:** nada
> **Maps to (roadmap):** Fase 1 — saídas do macerador · **Reused by:** [`0104-sawmill.md`](0104-sawmill.md) (serragem), [`0105-alloy-smelter.md`](0105-alloy-smelter.md) (escória)

O Pulverizer clássico não apenas dobrava o minério — tinha uma *chance* de um subproduto bônus (ex.: minério → 2 pós + às vezes o pó de um segundo metal ou uma gema). O Macerador atualmente produz uma única saída determinística. Isso adiciona a segunda saída baseada em chance e, ao fazer isso, constrói um **mecanismo reutilizável de saída por chance** que a Serraria e o Alloy Smelter também precisam.

## Problem & goal

O dobramento de minério está implementado; a *economia de subprodutos* que tornava o processamento de minérios do TE interessante, não. Adicionar uma saída secundária faz com que as cadeias de processamento valha otimizar (mais uptime = mais subprodutos raros) e cria um sumidouro natural para subprodutos de metais cruzados.

**Goal:** saída secundária opcional por receita com uma chance de drop independente, exposta no JEI, balanceada para enriquecer (não trivializar) o rendimento de minério.

## Requirements

### Functional
- Uma receita de Macerador pode declarar **zero ou uma saída secundária**: um item + uma `chance` em `[0,1]`.
- **Slot de saída secundária dedicado.** O Macerador ganha um terceiro slot: entrada (0) + saída primária (1) + **saída secundária (2)**. A secundária nunca compartilha nem bloqueia a primária.
- **Roll é por operação.** Na conclusão, a primária é produzida como hoje; a `chance` da secundária é rolada independentemente — sucesso a deposita no slot 2, falha deixa o slot 2 intocado.
- **Pausa-até-limpar (nenhum subproduto perdido).** Uma operação só completa quando **ambos** o slot primário *e* o secundário puderem aceitar seus resultados; se o slot secundário estiver cheio, o processamento **pausa** (o progresso se mantém, a entrada não é consumida) até que drene. O roll ainda decide se a secundária é depositada de fato, mas o espaço é exigido primeiro — então um slot secundário cheio paralisa o dobramento de minério (o trade-off aceito para nunca perder um subproduto).
- O JEI mostra a secundária com sua chance (ex.: "25%").
- **Compatível com versões anteriores:** receitas sem bloco `secondary` usam apenas o slot 1; o slot 2 fica vazio e nunca bloqueia. Maceradores salvos existentes (inventário de 2 slots) **redimensionam para 3 slots** no carregamento, com o novo slot secundário vazio.

### Balance
- Chances secundárias modestas (âncora da era TE: ~5–25% por operação, subprodutos raros menores).
- Subprodutos devem ser *úteis mas não fontes primárias* — uma forma de obter pequenas quantidades de um metal alternativo, não de substituir mineração.
- Sem mudança no tempo de moagem ou custo de energia por ter uma secundária (manter baixo o número de variáveis para v1).

## Design sketch

Estender o modelo de dados da receita, o layout dos slots, o GUI e a lógica de processamento pura — a receita e o plano já estão isolados para testes.

- **`MaceratorRecipeWrapper`** (`core/macerator/MaceratorRecipeWrapper.java`): adicionar campos opcionais `ItemStackTemplate secondaryResult` + `float secondaryChance` (padrão vazio/0). Atualizar o `MapCodec`/`StreamCodec` em `MaceratorRecipeSerializer` para ler um objeto opcional `secondary: { id, count, chance }`.
- **Slots da block entity** (`MaceratorBlockEntity`): crescer `ItemInventoryComponent` de 2 → **3 slots** (entrada / primária / secundária). `WorldlyContainer`: topo + lados → entrada (slot 0); **baixo → ambas as saídas (slots 1 *e* 2)** para extração. Tratar carregamento de NBT legado de 2 slots (preencher até 3).
- **`MaceratorProcessingPlan`** (`core/macerator/MaceratorProcessingPlan.java`): o gate de conclusão `acceptsOutput` deve agora verificar que **ambos** os slots primário e secundário têm espaço (isso implementa a pausa-até-limpar). O *roll* permanece deterministicamente testável — passar uma fonte aleatória (ou um booleano pré-rolado) em vez de chamar `level.random` dentro do plano, para que os testes de unidade assert ambas as branches. O plano reporta "secundária produzida: sim/não"; o BE aplica no slot 2.
- **GUI** (`MaceratorScreenHandler` + cliente `MaceratorScreen`): adicionar o segundo slot de saída e uma **barra/label de chance**; é uma mudança visível de textura/layout na tela do Macerador.
- **`MaceratorRecipeDisplay`** + **categoria JEI** (`core/macerator/jei/MaceratorRecipeCategory.java`): renderizar o slot secundário com um label de chance.
- **Recipe JSON** (`data/logistics/recipe/macerator/*.json`): nova forma opcional —
  ```json
  {
    "type": "logistics:macerator",
    "ingredient": "...",
    "result": { "id": "...", "count": 2 },
    "secondary": { "id": "...", "count": 1, "chance": 0.15 },
    "grindingtime": 200
  }
  ```

**Generalizar:** fatorar o "resultado secundário opcional baseado em chance" em um pequeno record compartilhado (ex.: `core/lib/recipe/ChanceResult`) para que os tipos de receita da Serraria e do Alloy Smelter embutam o mesmo campo com forma JSON idêntica e renderização JEI. O shape de **slot secundário dedicado + pausa-até-limpar** também se generaliza — Serraria (serragem) e Alloy Smelter (escória) adotam o mesmo layout de 3 slots e o gate de conclusão de ambas as saídas.

## Scope & non-goals

- **In:** uma saída secundária opcional por receita, chance por operação independente, o slot secundário dedicado + mudança de GUI, semântica pausa-até-limpar, exibição JEI, o helper compartilhado `ChanceResult`, migração de slot legado 2→3.
- **Adiado (não bloqueador):** o **mapa de conteúdo de subprodutos** real (qual minério dá qual metal alternativo, e em que %) — chega junto com o set de liga/material em [`0105-alloy-smelter.md`](0105-alloy-smelter.md). Esta feature entrega o mecanismo mais um ou dois secundários ilustrativos usando itens existentes.
- **Out:** múltiplas secundárias, escalonamento por fortuna/sorte, chances modificadas por upgrade (isso é [`0106-machine-upgrades.md`](0106-machine-upgrades.md) — o augment "secundário" se conecta aqui depois), roteamento lateral por saída (ambas as saídas compartilham a face inferior).

## Decisions

Todas as questões de nível de mecanismo estão resolvidas — o *conteúdo* de subprodutos é a única peça adiada, e é dado, não um bloqueador:

- **Slots de saída** — **slot secundário dedicado** (entrada / primária / secundária = 3 slots), estilo TE. A secundária nunca bloqueia a primária; o GUI ganha uma segunda saída + barra de chance; ambas as saídas extraem pela face inferior.
- **Comportamento quando slot cheio** — **pausa-até-limpar**: uma op só completa quando ambas as saídas têm espaço, então um slot secundário cheio paralisa o processamento em vez de perder o subproduto. Nenhum subproduto é jamais perdido.
- **Modelo de roll** — **por operação**, chance independente, deterministicamente testável (roll passado para `MaceratorProcessingPlan`).
- **Conteúdo de subprodutos** — **adiado para [`0105-alloy-smelter.md`](0105-alloy-smelter.md)**: entregar o mecanismo + `ChanceResult` + alguns secundários ilustrativos (itens existentes) agora; finalizar a tabela de subprodutos por minério com o set de liga/material.

> As escolhas restantes são detalhes de implementação: layout de textura do GUI para o segundo slot + barra de chance, e os secundários ilustrativos exatos entregues na v1.

## Done when

- Uma receita com um bloco `secondary` deposita no slot 2 à chance configurada, verificada por testes de unidade em `MaceratorProcessingPlan` (branches de sucesso e falha do roll).
- Com o slot secundário cheio, o Macerador **pausa** (progresso se mantém, entrada não consumida) e retoma quando drena — nenhum subproduto perdido. Testado com testes de unidade.
- JEI e o GUI da máquina mostram a secundária + chance.
- Receitas legadas (sem secundária) e Maceradores salvos existentes carregam inalterados (inventário preenche 2→3 slots).
- O helper compartilhado `ChanceResult` está em `core.lib` e consumido pelo Macerador (Serraria/Alloy o adotam em seus docs).

## References

- Roadmap: [`../roadmap.md`](../roadmap.md) → Fase 1 → saídas do macerador
- Detalhamento: [`../mods/thermal-expansion.md`](../mods/thermal-expansion.md) → linha "saída secundária/subproduto"
- **Nota de fonte TE:** o Thermal Expansion **não tem repositório público** — o comportamento do Pulverizer (slot secundário dedicado, chance por operação, faixa ~5–25%) é reconstruído a partir de conhecimento + wiki do TE. Tratar chances exatas como *aproximadas* e ajustar em playtest; não citar números de linhas de fonte.
- Código: `core/macerator/{MaceratorRecipeWrapper,MaceratorRecipeSerializer,MaceratorProcessingPlan,MaceratorRecipeDisplay}.java`, `core/macerator/jei/MaceratorRecipeCategory.java`, receitas em `data/logistics/recipe/macerator/`
