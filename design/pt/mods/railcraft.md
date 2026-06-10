# Railcraft

*Trilhos, minecarts avançados, sinais e a cadeia de processamento de vapor/aço — transporte inter-base e logística em massa. Totalmente greenfield e o módulo mais claramente separável. Esta é a Fase 3.*

**Era de origem:** 1.7.10–1.12.2 (Railcraft).
**Módulo Logistics:** `logistics-transport` (novo) — o candidato mais forte a standalone.
**Fase:** 3.

Veja [`../principles.md`](../principles.md) para a legenda da tabela. Railcraft é *enorme*; as decisões abaixo **consolidam a enorme taxonomia de trilhos/carts** em um conjunto coerente e reconsideram seus muitos multiblocos.

## Trilhos

| Feature | What it did (1.7.10–1.12.2) | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
| Trilho básico/reforçado/de madeira/alta velocidade | Trilhos em níveis (durabilidade, velocidade) | Modernize | Manter um pequeno conjunto de níveis: básico → reforçado → alta velocidade | — | Fase 3 — trilhos |
| Switch / junção / wye / turnout | Controle de direção | Port | Essencial para qualquer rede ferroviária | — | Fase 3 — trilhos |
| Trilho motorizado / impulsionador | Acelerar carts (substitui trilho motorizado vanilla em escala) | Port | Movimento essencial | — | Fase 3 — trilhos |
| Trilhos detectores | Emitir redstone nas condições do cart | Port | Cola de automação | — | Fase 3 — trilhos |
| Trilhos de controle (boarding/holding/locking/one-way/buffer/launcher) | Controle fino de cart + diversão (launcher) | Modernize | Consolidar os 12+ trilhos de controle em um conjunto menor e configurável | — | Fase 3 — trilhos |
| Trilho de roteamento + bilhete | Rotear carts por destino | Port | Combina naturalmente com o tema de logística | — | Fase 3 — roteamento |
| Trilho elétrico | Alimentar locomotivas elétricas | TBD | Só se locomotivas elétricas estiverem no escopo | — | Fase 3 — trilhos |

## Minecarts

| Feature | What it did | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
| Locomotiva (vapor / elétrica / criativa) | Motor auto-alimentado que puxa carts | Port | O destaque: trens automatizados. Começar com vapor | — | Fase 3 — locomotivas |
| Cart de tanque | Transporte móvel de fluidos | Port | Depende da camada de fluidos | — | Fase 3 — carts |
| Carts de baú / trabalho (energia/âncora) | Inventário móvel + carts utilitários | Port | Transporte de armazenamento móvel; combina com carregadores | — | Fase 3 — carts |
| Carregadores / descarregadores (item & fluido) | Carregar/descarregar carts automaticamente em estações | Port | Conecta trilhos às redes de tubo/fluido | — | Fase 3 — carregadores |
| Carts divertidos (TNT / presente / undercutter) | Carts de novidade/utilidade | Modernize | Manter alguns (cart TNT); descartar o resto | — | Fase 3 — carts |
| Tunnel Bore | Tunelamento automático montado em cart | Modernize | Legal mas complexo; possível alternativa/junto com o Laser Quarry | — | Mais tarde |
| Dispensador de cart | Colocar carts automaticamente no trilho | Port | Automação de estação | — | Fase 3 — carregadores |

## Sinais

| Feature | What it did | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
| Sinais de bloco / distante / token + controladores/receptores | Espaçamento e controle automatizados de trens | Modernize | Alto valor para redes automatizadas, mas historicamente complicado — **simplificar** em um conjunto de sinais mais acessível | — | Fase 3 — sinais |
| Sintonizador de sinal | Parear sinais | Port | Necessário se sinais forem entregues | — | Fase 3 — sinais |

## Vapor, aço & processamento em massa

| Feature | What it did | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
| Forno de Coque | Multibloco: carvão → coque + óleo de creosoto | Modernize | Coque + creosoto alimentam aço + combustível/madeira tratada. **Reconsiderar o multibloco** — preferir bloco único ou ladrilhável (escala opcional) | — | Fase 3 — cadeia de vapor |
| Alto-forno | Multibloco: ferro + coque → aço | Modernize | Aço é o material chave do módulo; **máquina de bloco único** preferida à estrutura de 34 blocos | — | Fase 3 — aço |
| Caldeira a vapor (sólido/líquido, HP/LP) | Multibloco: combustível + água → vapor | Modernize | Vapor como nível de energia; grande multibloco — reconsiderar escala vs. tédio; se acopla a `power` | — | Fase 3 — energia a vapor |
| Turbina a vapor / motores a vapor | Vapor → RF/trabalho | Modernize | Unificar com a linha de motores (motor nível vapor) | — | `power` / engines |
| Tanque de ferro / aço | Armazenamento de fluido em massa multibloco | Modernize | O papel clássico dos "Iron Tanks". *Escala é a feature* — exceção válida de multibloco, ou blocos de tanque ladrilháveis | — | Fase 3 — tanques (fluidos) |
| Rock Crusher | Processamento de minérios multibloco | Skip | Coberto pelo Macerador | ❌ | [`thermal-expansion.md`](thermal-expansion.md) |
| Rolling Machine | Crafting de cart/trilho | Modernize | Dobrar crafting de trilho em receitas normais/vanilla-Crafter | — | — |
| Tanque de água (multibloco) | Coletar água para caldeiras | Modernize | Só se caldeiras precisarem; preferir simples | — | — |

## Materiais & utilidade

| Feature | What it did | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
| Aço | Liga de alto-forno; trilhos/ferramentas | Port | Material principal do `logistics-transport` | — | Fase 3 — aço |
| Óleo de creosoto | Preservante de madeira (madeira tratada) + combustível | Port | Do forno de coque; madeira tratada para trilhos/dormentes | — | Fase 3 — cadeia de vapor |
| Coque / carvão vegetal | Combustível de fundição | Port | Do forno de coque | — | Fase 3 — cadeia de vapor |
| World Anchor (chunkloading) | Manter chunks de cart/quarry carregados | TBD | Útil para quarries/fazendas mas sensível (performance do servidor); precisa de Discussion | — | Mais tarde |
| Gravação / emblemas / firestone / miscelânea | Extras cosméticos e de nicho | Skip | Fora do escopo | ❌ | — |

> TODO: as decisões sobre multiblocos (forno de coque, alto-forno, caldeira, tanque) são a maior tensão de design neste módulo vs. a posição anti-multibloco — decidir por bloco (único vs. ladrilhável vs. multibloco real) via Discussion antes de agendar. Veja [RFC 0003 — Multiblocos & Vapor do Railcraft](../rfcs/0003-railcraft-multiblocks.md).
> TODO: confirmar o escopo da cadeia de energia a vapor — se vapor é um nível real em `power` (caldeira→turbina→RF) ou simplificado; se acopla à unificação da linha de motores em [`buildcraft.md`](buildcraft.md) / [`thermal-expansion.md`](thermal-expansion.md). Veja [RFC 0003 — Multiblocos & Vapor do Railcraft](../rfcs/0003-railcraft-multiblocks.md).
> TODO: confirmar se locomotivas/trilhos elétricos estão no escopo ou se é somente vapor para v1.
