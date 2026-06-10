# Remote Orderer

> **Status:** 🚧 Planned · **Phase:** 1 — Automation core · **Module:** `logistics-automation` (domínio `pipe`)
> **Source:** [`../mods/logistics-pipes.md`](../mods/logistics-pipes.md) (Remote Orderer) · **Depends on:** nada
> **Maps to (roadmap):** Fase 1 — logistics QoL

Um item de mão que abre a UI de requisição de rede de qualquer lugar no alcance — peça itens à sua rede logística sem estar parado em um tubo Requester. Um Modernize de alto valor de QoL: a UI de requisição e o fluxo de pedido já existem; isto é um novo item que os alcança.

## Problem & goal

Hoje, requisitar itens significa interagir com um tubo Requester em um ponto fixo. O Remote Orderer do Logistics Pipes deixava você puxar da rede em movimento — uma conveniência definidora do sistema.

**Goal:** um item de mão que abre a tela de requisição existente contra uma rede escolhida/próxima, balanceado por alcance e/ou custo, reutilizando a maquinaria de pedido/despacho integralmente.

## Requirements

### Functional
- Clique-direito abre a **UI de requisição** (mesmo fluxo do tubo Requester: navegar itens disponíveis da rede, buscar, escolher quantidade, fazer um pedido).
- O item tem como alvo uma rede por um **`BlockPos` de tubo armazenado** — shift-clique-direito em um tubo para vinculá-lo (salvo no item via data component). *(Um fallback "rede mais próxima" precisaria escanear um raio manualmente por um `BlockPos` de tubo — o `NetworkRegistry` não tem lookup de tubo-mais-próximo — então vinculação é o caminho principal; veja Questões em aberto.)*
- Pedidos passam pelo caminho normal `ILogisticsNetwork.placeOrder(item, amount, requester, FulfillmentMode)`. **`requester` = o `BlockPos` do tubo vinculado** (a entrega roteia para esse tubo / seu inventário conectado — o orderer é um *gatilho* remoto, não um teletransporte). **`FulfillmentMode` = permitir `PARTIAL`** (entregar o que estiver disponível agora), combinando com o comportamento do tubo Requester — confirmar contra seu default real. Liga-se à questão de entrega em aberto.
- Funciona apenas quando a rede alvo está carregada e dentro do alcance; feedback claro quando fora do alcance / não vinculado / sem rede.

### Balance
- **Limitado por alcance** — não um terminal sem fio de toda a base. Um raio generoso-mas-limitado (ex.: dezenas de blocos) o mantém como conveniência, não teletransporte.
- Opcionalmente um **custo de energia/carga** ou custo de crafting usando componentes de nível médio para que seja uma ferramenta conquistada, não item inicial.
- A entrega precisa cair *em algum lugar físico* — pedidos cumprem para um pipe/inventário, não magicamente na mochila do jogador.

## Design sketch

O lado de requisição está completamente construído — `RequesterScreenHandler` já consulta a rede por itens disponíveis e faz pedidos; `RequesterModule.onWrench` mostra o idioma exato de `openMenu`. O novo trabalho é um **item** que alcança uma rede por posição em vez de ser um pipe.

```
common/src/main/java/com/logistics/pipe/item/RemoteOrdererItem.java
```

- `RemoteOrdererItem extends Item`:
  - `use(level, player, hand)`: **somente no servidor** (`openMenu` é server-side de qualquer forma). Ler o `BlockPos` vinculado de `DataComponents.CUSTOM_DATA`; verificar alcance; resolver a rede a partir dessa posição (abaixo); então `serverPlayer.openMenu(new SimpleMenuProvider(... new RemoteRequesterScreenHandler(syncId, inv, targetPos), title))`. *(Nenhuma API "mais próximo" do `NetworkRegistry` existe — um fallback de rede mais próxima opcional deve escanear manualmente um raio por um `BlockPos` de tubo.)*
  - `useOn(...)` / shift-use em um pipe: vincular o orderer ao tubo, armazenar no item via `ItemStack.set(DataComponents.CUSTOM_DATA, ...)` (precedente de data-component: `pipe/data/PipeDataComponents`).
- **`RemoteRequesterScreenHandler`**: variante fina de `RequesterScreenHandler` que recebe um `BlockPos` e resolve a rede **no servidor**. `NetworkRegistry.getNetwork(level, pos)` é um lookup **somente leitura** (retorna null para posições não mapeadas / no cliente); usar `NetworkRegistry.getOrCreateNetwork(level, pos)` no caminho servidor onde a posição pode ainda não estar mapeada. Reutilizar a lógica existente de browse/busca/pedido de itens.
- Registrar o item em `LogisticsPipe.ITEM` e o menu no registrador de menus.

## Scope & non-goals

- **In:** o item de mão, vinculação de rede via data component (ou fallback de rede mais próxima), UI de requisição contra rede remota, tratamento de alcance/feedback.
- **Out:** terminais sem fio entre dimensões / alcance ilimitado, redesenhar a UI de requisição.

## Open questions

- **Modelo de alvo: vinculado vs. mais próximo.** Vinculação é previsível; mais próximo é zero-config mas ambíguo com alcances sobrepostos. **Inclinação: vincular, com o item mostrando alvo vinculado no tooltip.**
- **Destino de entrega.** Para onde vão os itens pedidos? **Inclinação: pedidos cumprem para o alvo de entrega existente da rede; o orderer é um *gatilho* remoto, não um *teletransporte* remoto.**
- **Custo/gate:** nível de receita de crafting, e se consome energia/carga por uso.
- Valor de alcance e se escala com algo.

## Done when

- O item abre a tela de requisição contra uma rede vinculada/próxima carregada dentro do alcance, em ambos os loaders.
- Fazer um pedido roteia itens pelo caminho normal de despacho para o destino de entrega acordado.
- Estados fora-de-alcance / não-vinculado / não-carregado dão feedback claro ao jogador.
- Vinculação persiste no item através de empilhamento/drops.

## References

- Roadmap: [`../roadmap.md`](../roadmap.md) → Fase 1 → logistics QoL; [`../mods/logistics-pipes.md`](../mods/logistics-pipes.md) → linha Remote Orderer
- Código: `pipe/ui/RequesterScreenHandler`, `pipe/modules/RequesterModule#onWrench`, `pipe/item/ModuleItem#use`, `pipe/network/NetworkRegistry`, `core/lib/network/{ILogisticsNetwork,Order}`, `pipe/data/PipeDataComponents`, `LogisticsPipe.java`
