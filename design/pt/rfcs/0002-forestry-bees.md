# RFC 0002: Abelhas Forestry — Se e Como

> **Status:** 🟡 Open — precisa de Discussion de Ideas/Polls antes de qualquer trabalho de abelhas da Fase 2 · **Scope:** Fase 2 (Forestry) · **Decides:** mantenedor + sinal de demanda da comunidade
> **Affects:** [`../mods/forestry.md`](../mods/forestry.md) § Abelhas (todas as linhas TBD) · **Blocks:** agendar qualquer trabalho de apicultura — **não** fazendas, árvores, processamento ou eletrônica

Abelhas são a parte do Forestry mais carregada de nostalgia e mais *divergente*. Este RFC resolve se acontecem de alguma forma para v1, e em qual modelo — antes de qualquer coisa ser agendada.

## Contexto

A apicultura do Forestry era um sistema profundo: ciclo de vida **princesa/zangão/rainha**, **genética Mendeliana** (traços, mutações, descoberta de espécies), o Apiário/Alveary, e uma centrífuga transformando favos em mel/cera/geleia.

O problema: **o vanilla moderno já tem abelhas**, criadas alimentando-as com flores. Esse modelo diverge muito da genética baseada em rainha do Forestry. Reconciliá-los é uma bifurcação real de design, e a genética do Forestry era famosamente dependente de wiki — em tensão com [`../principles.md`](../principles.md) ("modernizar para encaixar no vanilla", "aprendível sem wiki", e a posição sobre multiblocos, que o Alveary viola).

[`../mods/forestry.md`](../mods/forestry.md) já afirma que abelhas **não são destaque da Fase 2 e podem ser puladas inteiramente para v1**, e que isso precisa de uma passagem de design deliberada. Fazendas são o destaque; abelhas não devem bloqueá-las.

## A decisão a tomar

**Para v1, o Logistics entrega abelhas de alguma forma — e se sim, estendemos as abelhas vanilla ou construímos um sistema de genética paralelo?**

## Opções

### Opção A — Pular abelhas para v1 *(inclinação)*
Entregar `logistics-forestry` sem apicultura. Revisitar pós-1.0 se houver demanda.
- **Prós:** remove o subsistema mais difícil, mais divergente e mais dependente de wiki do caminho crítico; deixa o Forestry entregar em seu destaque real (fazendas/árvores/processamento); corresponde à inclinação declarada no detalhamento.
- **Contras:** "Forestry sem abelhas" decepciona a galera da nostalgia para quem abelhas *eram* o Forestry.

### Opção B — Estender as abelhas vanilla *(o caminho modernizado se perseguido)*
Construir sobre a colmeia/ninho-de-abelhas vanilla + criação por flores. Adicionar **produtos** com sabor Forestry (favos → centrífuga → cera/geleia/mel) e traços *leves*, **sem** o ciclo de vida princesa/zangão/rainha.
- **Prós:** encaixa em "modernizar para o vanilla"; reutiliza um sistema que jogadores já entendem; entrega as *saídas* reconhecíveis (produtos da centrífuga) sem o fardo da genética.
- **Contras:** não são abelhas Forestry "de verdade"; puristas podem achar raso; ainda trabalho não trivial.

### Opção C — Genética paralela completa
Reimplementar princesa/zangão/rainha + mutações Mendelianas + descoberta de espécies como um sistema standalone.
- **Prós:** a recreação mais fiel; o endgame mais profundo.
- **Contras:** altíssima complexidade; conflita com as abelhas vanilla (dois sistemas paralelos); maximamente dependente de wiki; o Alveary puxa um multibloco. Maior risco contra os princípios.

## Recomendação / inclinação

**A para v1**, com **B como o caminho modernizado eventual** se abelhas forem perseguidas pós-1.0. **C é improvável** — conflita com múltiplos princípios (modernizar-para-vanilla, aprendível-sem-wiki, posição sobre multiblocos) e duplica um sistema que o vanilla agora possui. Mas isso é baseado em gosto e demanda, então deve ir para um poll em vez de ser definido como padrão.

## Sub-questões ainda em aberto

- **Sinal de demanda:** jogadores realmente querem abelhas baseadas em rainha clássicas, ou nostalgia de um sistema que envelheceu mal? (Este é o ponto central — colocar em poll.)
- Se B: quão profundos vão os traços/produtos? Apiário como novo bloco vs. puramente estender a colmeia vanilla?
- O **Alveary** (multibloco avançado) é Skip independente de A/B/C — confirmar e registrar.
- Algo mais no Forestry depende de produtos de abelhas (ex.: receitas do Carpenter usando mel/cera)? Se sim, obter esses de outra forma na Opção A.

## Como decidiremos

Discussion de Ideas/**Polls** — abelhas são carregadas de nostalgia, então sinal de reação/voto é o indicador certo. Decidir **antes** de qualquer agendamento de abelhas da Fase 2. O resultado muda as linhas de Abelhas em `forestry.md` de TBD para Modernize/Skip e, se B, semeia um feature brief.

## Referências

- Detalhamento: [`../mods/forestry.md`](../mods/forestry.md) § Abelhas (adiado) + o TODO de abelhas
- Roadmap: [`../roadmap.md`](../roadmap.md) → Fase 2 → "Abelhas (adiado / incerto)"
- Princípios em tensão: [`../principles.md`](../principles.md) (modernizar-para-vanilla, aprendível-sem-wiki, posição sobre multiblocos)
