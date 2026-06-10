# Firewall Pipe

> **Status:** 🚧 Planned · **Phase:** 1 — Automation core · **Module:** `logistics-automation` (domínio `pipe`)
> **Source:** [`../mods/logistics-pipes.md`](../mods/logistics-pipes.md) (Firewall pipe — segmentação de rede) · **Depends on:** nada
> **Maps to (roadmap):** Fase 1 — logistics avançado (Firewall pipe)

Um tubo que segmenta uma rede logística — controlando o que (itens, requisições, anúncios de provider, crafting) pode cruzar entre duas sub-redes. A ferramenta para manter bases grandes sãs: isolar uma sub-base sem desconectá-la fisicamente.

## Problem & goal

Quando uma rede fica grande, tudo vê tudo: providers anunciam para toda a base, requisições roteiam para qualquer lugar, sorting se expande. O Firewall do Logistics Pipes deixava colocar uma fronteira controlada para que duas metades de um run contíguo de tubos se comportem como redes quase separadas.

**Goal:** um tubo que age como fronteira configurável unidirecional/seletiva entre segmentos de rede, para que builders possam particionar uma rede sem cortá-la. Esta é a mais sensível ao design das três features de pipe porque toca o **grafo de rede**, então a escolha de abordagem importa.

## Requirements

### Functional
- Um Firewall pipe fica em um run de pipe e **filtra o que cruza** entre os dois lados. Configurável (toggles por categoria): bloquear/permitir **itens**, **requisições**, **anúncios de provider**, propagação de **crafting** através da fronteira.
- Direcionalidade: no mínimo um bloqueio simétrico; idealmente por direção (permitir A→B, bloquear B→A) como um firewall real.
- Configurado via wrench/GUI; estado persiste no pipe BE.
- Os dois lados permanecem fisicamente conectados (ainda é um tubo) mas logicamente particionados pelas regras.

### Balance
- Um bloco puramente organizacional/QoL — sem custo de energia, sem mudança de throughput. Seu valor é controle, não velocidade.
- Não deve criar dead-ends de roteamento ou itens perdidos: itens bloqueados não devem sumir — devem simplesmente não receber uma rota de cruzamento (ficar/retornar), ou ser rejeitados de forma limpa.

## Design sketch

**Duas abordagens viáveis** — esta é a decisão chave a tomar antes de construir.

**(A) Módulo routing-gate (mais leve).** Um `FirewallModule implements RoutingModule` (+ estado de config) que vive em um pipe normal. A rede permanece um grafo único; o módulo veta cruzamentos no tempo de roteamento.
- *Prós:* sem mudanças em `NetworkGraph`/`PipeNetwork`; usa os hooks `Module` + `RoutingModule` existentes; entrega rápido.
- *Contras:* "segmentação" é emulada na camada de roteamento, não na topologia — filtragem de *anúncio* de provider/requisição precisa que o controlador de rede consulte o firewall ao longo dos caminhos, o que o `NetworkController`/`NetworkGraph` atual pode não expor de forma limpa.

**(B) Segmentação real de grafo (mais pesada, mais correta).** Tratar pipes de firewall como arestas tipadas no grafo para que `NetworkPathfinder`/`NetworkGraph` calcule alcançabilidade *sujeita a* regras de firewall.
- *Prós:* semanticamente correto; visibilidade de provider e roteamento de requisição naturalmente respeitam a fronteira.
- *Contras:* invasivo — mudanças em `INetworkGraph`/`NetworkGraph`/`NetworkController`; risco a um sistema atualmente completo e estável.

**Recomendação:** começar com **(A)** para bloqueio de *fluxo de itens* e *requisições* (os comportamentos mais desejados), e explicitamente escopar particionamento de anúncio-de-provider como a parte que pode precisar de uma fatia de **(B)**. Validar contra os caminhos reais de query de requisição/provider do `NetworkController` antes de comprometer. **Decidir via spike curto antes de agendar a feature completa.**

## Scope & non-goals

- **In:** um pipe de fronteira configurável; toggles de cruzamento por categoria (itens, requisições, anúncios, crafting) na medida em que a abordagem escolhida suporta; por-direção se viável.
- **Out:** segurança/permissões por jogador (Security Station pulado), criptografia/bloqueio, canais de firewall múltiplos nomeados, fronteiras entre dimensões.

## Open questions

- **Abordagem (A) routing-gate vs. (B) segmentação de grafo** — a decisão central. Precisa de spike contra `NetworkController`/`NetworkGraph` para confirmar se filtragem de anúncio-de-provider é viável em (A). **Resolver antes de agendar.**
- Quais categorias de cruzamento chegam em v1 — provavelmente **itens + requisições** primeiro, anúncios/crafting como follow-up mais difícil.
- Config simétrica vs. por-direção para v1. **Inclinação: simétrico primeiro.**
- Semântica de falha exata para item bloqueado em trânsito (retornar ao remetente? segurar? rejeitar na face da fronteira?).

## Done when

- Um Firewall pipe em um run contíguo impede as categorias configuradas de cruzar, em ambos os loaders.
- Itens bloqueados não são perdidos (rejeição/retorno limpo).
- Config persiste e é editável via wrench/GUI.
- O comportamento é documentado honestamente como emulado-via-roteamento vs. real-topológico conforme a abordagem escolhida.

## References

- Roadmap: [`../roadmap.md`](../roadmap.md) → Fase 1 → logistics avançado; [`../mods/logistics-pipes.md`](../mods/logistics-pipes.md) → linha Firewall pipe
- Código: `core/lib/pipe/{Module,RoutingModule}`, `pipe/modules/*`, `pipe/network/{NetworkRegistry,PipeNetwork,NetworkController}`, `core/lib/network/{INetworkGraph,NetworkGraph,NetworkPathfinder,ILogisticsNetwork}`, `pipe/ui/RequesterScreenHandler`, `pipe/PipeTypes`, `LogisticsPipe.java`
