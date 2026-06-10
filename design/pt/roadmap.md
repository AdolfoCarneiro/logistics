# Roadmap

O plano por fases do dia zero até "completo." As fases são sequenciadas para **construir sobre os pontos fortes existentes primeiro**: o núcleo de automação BuildCraft / Logistics Pipes / Thermal Expansion está em grande parte no lugar, então o completamos antes de abrir as fronteiras do Forestry e Railcraft.

Este arquivo é o espelho legível por humanos do **GitHub Project #4 "Logistics Roadmap."** É escrito aqui primeiro; seções consolidadas são decompostas no quadro (veja [Mapeamento para o quadro](#mapeamento-para-o-quadro)).

> Os conteúdos por fase são preenchidos a partir dos detalhamentos em [`mods/`](mods/). Até um detalhamento ser finalizado, as linhas de sua fase são provisórias.

## Visão geral das fases

| Fase | Tema | Mods de origem | Estado |
|---|---|---|---|
| **0 — Fundação** | Materiais, ferramentas, energia, a rede de tubos | Logistics Pipes, base do BuildCraft/TE | ✅ Amplamente feito |
| **1 — Núcleo de automação** | Completar motores, processamento de minérios, máquinas RF, quarry, gates/automação | BuildCraft + Thermal Expansion | 🚧 Em andamento |
| **2 — Forestry** | Automação biológica: abelhas, árvores, fazendas, eletrônica | Forestry | — Não iniciado |
| **3 — Transporte** | Trilhos, minecarts avançados, tanques, sinais, processamento em massa | Railcraft | — Não iniciado |

---

## Versionamento & 1.0

**1.0 é uma promessa de estabilidade, não a linha de chegada.** Não significa que todos os cinco mods estão prontos — essa é a visão de longo prazo. 1.0 significa que uma fatia coesa e auto-contida está completa e vamos mantê-la.

**1.0 = o núcleo de automação (Fase 1) completo e estável, em ambos os loaders.** Isso é BuildCraft + Logistics Pipes + Thermal Expansion — um mod completo e reconhecível por si só. Forestry (`logistics-forestry`) e Railcraft (`logistics-transport`) são módulos **pós-1.0** que chegam como minors 1.x posteriores.

### Definição de pronto para 1.0

1. **Fase 1 com features completas** — as lacunas do BC/TE preenchidas (fundação de fluidos, motor nível combustão, alloy smelter, serraria, saídas secundárias do macerador, upgrades de máquinas). Logistics Pipes já está feito.
2. **Paridade de loaders** — NeoForge entregue, não "em andamento." Fabric + NeoForge ambos no nível exigido.
3. **Estabilidade de formato** — NBT de bloco/BE, data components e config consolidados; **sem mudanças que quebrem saves esperadas**. Esse é o significado real do 1.0.
4. **Polimento** — cobertura JEI/receitas, uma passagem de equilíbrio, docs atualizados, sem crashes conhecidos.

### Lançamento em sincronia

**1.0 é declarado somente quando *todas* as branches de versão MC suportadas (mc/1.21.1, mc/1.21.11, mc/26.1, …) e *ambos* os loaders atendem ao critério simultaneamente.** Nenhuma branch vai para 1.0 antes das outras. Essa é a promessa mais forte e mais lenta: um jogador em qualquer versão suportada recebe o mesmo núcleo 1.0 estável. (Pré-1.0, as branches ainda versionam independentemente em suas próprias linhas.)

### Mecânica de lançamento

- **Pré-1.0 (agora, 0.6.x):** `feat:` → patch, `feat!:` → minor (conforme config do release-please). Queimar a Fase 1 + NeoForge como minors 0.7–0.9.
- **A virada no 1.0:** depois disso `feat:` → **minor** e `feat!:` → **major** — para que breaking changes fiquem em evidência. Não declarar 1.0 até estar disposto a evitar breaking changes ou aceitar bumps de major.
- **Gate:** publicar `1.0.0-pre.N` via o workflow de pré-release para testes finais entre versões/loaders antes de declarar `1.0.0`.

---

## Fase 0 — Fundação ✅

*A base. Majoritariamente completa — listada para que o roadmap comece do dia zero.*

- ✅ Abstração de energia agnóstica de loader (RF; adaptadores Team Reborn / NeoForge)
- ✅ Transporte de itens + simulação e renderização de item viajante
- ✅ Modelo de tubos em três camadas (Mecânico → Inteligente → Rede)
- ✅ Rede logística completa: provider / requester / supplier / crafting / process / satellite + chassis MkI–V & módulos
- ✅ Materiais base: estanho, bronze, apatita, pós, engrenagens, chips lógicos; ferramentas (chave inglesa, sonda)
- ✅ Motores (redstone / stirling / criativo) com estágios de calor; cabos (cobre/ouro/ender); bateria
- ✅ Macerador (moagem RF + receitas customizadas + JEI), Forno (forno RF), Laser Quarry (delimitado por marcadores)

> Detalhes e lacunas restantes ficam em [`mods/logistics-pipes.md`](mods/logistics-pipes.md) e nas porções da Fase 0 de [`mods/buildcraft.md`](mods/buildcraft.md) e [`mods/thermal-expansion.md`](mods/thermal-expansion.md).

## Fase 1 — Núcleo de automação 🚧

*Terminar a camada BuildCraft + Thermal Expansion sobre a qual a experiência tech clássica foi construída. Derivada de [`mods/buildcraft.md`](mods/buildcraft.md), [`mods/thermal-expansion.md`](mods/thermal-expansion.md), e das lacunas em [`mods/logistics-pipes.md`](mods/logistics-pipes.md).*

**🔑 Fundação de fluidos** *(pré-requisito para muito desta fase + fases posteriores)*
- Camada de transporte de fluidos (tubos de fluido) na API de fluidos da plataforma
- Armazenamento de fluidos (tanques) + I/O de fluidos nas máquinas
- Bomba (fluido do mundo → rede)

**Motores & energia**
- Motor nível combustão (combustível líquido + refrigerante, tensão de gerenciar-ou-explodir)
- Unificar dínamos/geradores na linha de motores (nível "magmático" com combustível líquido)
- Bateria → linha de armazenamento de energia em camadas com I/O configurável

**Processamento de minérios & máquinas**
- Saídas secundárias/subprodutos do macerador (baseadas em chance)
- Alloy Smelter (Bronze/Invar/Electrum + o conjunto de materiais de ligas)
- Serraria (toras → pranchas + serragem)
- Upgrades / augments de máquinas (velocidade / eficiência / secundário / saída automática)
- Magma Crucible + Fluid Transposer *(precisa de fluidos)*

**Tubos & QoL de logística**
- Gating de energia para operação de tubos *(em andamento)*
- Remote Orderer (acesso à rede portátil)
- Tubo vácuo de obsidiana (coleta de itens do mundo)
- Tubo firewall (segmentação de rede)
- Logística de fluidos: provider/supplier/requester líquido *(precisa de fluidos)*

**Combustíveis** *(se acopla ao Motor de Combustão + biocombustível do Forestry)*
- Decidir fonte de óleo/biocombustível; construir a cadeia de combustível

**Comportamento programável — explicitamente pós-1.0 (adiado)**
- O sistema unificado de gates + circuitos (gates do BuildCraft · **augments** programáveis do TE · **circuitos** do Forestry → uma camada de "comportamento programável") **não é um item de 1.0.** Adiado até o Forestry precisar — ou seja, circuit boards para programar blocos Forestry (Fase 2) — ou depois. Veja [RFC 0001](rfcs/0001-programmable-behavior.md).
- *Distinto dos **upgrades de máquinas** da Fase 1 (modificadores de velocidade/eficiência/saída-automática) na Definição de Pronto acima — esse é um sistema de modificadores, não a camada de lógica programável, e não é afetado por este adiamento.*

## Fase 2 — Forestry —

*Automação biológica e variedade no endgame. Derivada de [`mods/forestry.md`](mods/forestry.md). Apatita (fertilizante) e os "eletrônicos" de chip lógico/núcleo/válvula já estão semeados.*

**Fazendas** *(destaque)*
- Fazendas de bloco único (o design mais antigo do Forestry, **não** a multifazenda multibloco posterior) + seleção de tipo de fazenda/upgrades
- Pântano de turfa → combustível de turfa
- Cadeia de fertilizante sobre a Apatita existente

**Processamento & energia** *(segunda prioridade próxima)*
- Carpenter (modernizado — mais claro que o original)
- Squeezer → Fermenter → Still — cadeia de biocombustível *(combustível paralelo para o motor de combustão)*
- Bottler *(precisa de fluidos)*
- Motores de turfa / biocombustível unificados na linha de motores

**Árvores**
- Arboricultura (mudas/pólen, enxertador); produtos alinhados às madeiras vanilla + resina

**Eletrônica**
- Thermionic Fabricator (modernizado, idealmente unificado com o Carpenter) → tubos eletrônicos (mapeados para os núcleos/válvulas/chips lógicos existentes)
- Circuit boards + solda → integra ao RFC de comportamento programável da Fase 1

**Abelhas** *(adiado / incerto — pode ser pulado para v1)*
- A criação de flores vanilla diverge muito da princesa/zangão/rainha do Forestry; precisa de uma passagem de design (Discussão) antes de agendar

## Fase 3 — Transporte —

*Trilhos, minecarts e logística em massa entre bases. Derivada de [`mods/railcraft.md`](mods/railcraft.md). O módulo mais separável (`logistics-transport`).*

**Trilhos**
- Camadas de trilhos (básico → reforçado → alta velocidade)
- Switches/junções, alimentado/impulsionador, detectores
- Trilhos de controle consolidados; trilho de roteamento + bilhetes

**Locomotivas & minecarts**
- Locomotiva a vapor (trens automatizados)
- Minecarts de tanque / baú / trabalho *(cart de tanque precisa de fluidos)*
- Carregadores/descarregadores de itens & fluidos; dispensador de carts

**Sinais**
- Conjunto de sinais simplificado e acessível (bloco/distante) + sintonizador

**Cadeia de coque & aço**
- Forno de Coque (coque + creosoto) — reconsiderar multibloco
- Alto-forno → **Aço** (preferência por bloco único); creosoto → madeira tratada
- Energia a vapor (caldeira/turbina) — *TBD, se acopla à linha de motores*
- Tanques de fluido em massa — *TBD (exceção válida de multibloco?)*

> **Dependências transversais:** a **fundação de fluidos** (Fase 1) desbloqueia logística de fluidos, várias máquinas TE, engarrafamento de biocombustível (Fase 2) e carts de tanque/tanques (Fase 3). O **RFC de comportamento programável** abrange as Fases 1–2. Agende ambos cedo dentro de suas fases.

---

## Mapeamento para o quadro

Quando uma seção de detalhamento consolidar, decomponha no Project #4:

| Conceito do doc | Representação no Project #4 |
|---|---|
| **Fase** (0–3) | **Milestone** (ex.: "Fase 1 — Núcleo de automação") |
| **Área de mod / épico** (ex.: "Motores & energia") | **Issue épica** (issue pai) |
| **Linha de feature** em uma tabela `mods/*.md` | **Sub-issue** sob o épico (usa os campos *Parent issue* / *Sub-issues progress* do quadro) |
| **Decisão** (Port/Modernizar/Pular) | Label: `port` / `modernize` / `wontport` *(a criar)* + labels de escopo existentes |
| **Status** (✅/🚧/—/❌) | Campo **Status** do quadro; linhas ❌ não são arquivadas |

**Regras de decomposição:**
- Reconciliar com issues já no quadro (ex.: *"Forestry-Inspired Farming & Electronics Roadmap"*, *"BuildCraft compatibility shim"*) — estender/linkar, não duplicar.
- Arquivar apenas linhas aceitas (Port/Modernizar). Linhas de Skip ficam documentadas aqui como justificativa.
- Fazer um **dry-run de mapeamento em um arquivo de mod** antes de qualquer criação em massa de issues.
- Esse passo acontece **apenas com aprovação explícita** — o markdown é escrito e revisado primeiro.
