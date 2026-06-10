# Princípios de Decisão

Este arquivo define **como decidimos** o que trazer de cada mod de origem, e a tabela compartilhada que todo detalhamento em [`mods/`](mods/) usa. O objetivo é decisões consistentes e defensáveis que se somem em um todo equilibrado com sensação clássica.

## As três decisões

Cada feature de um mod de origem recebe exatamente uma decisão:

- **Portar** — trazer essencialmente como está. A mecânica ainda faz sentido no Minecraft moderno e encaixa no equilíbrio. (Pode ainda receber textura/receita nova.)
- **Modernizar** — manter a *intenção*, mudar a *forma* para encaixar no vanilla moderno. Use quando o original conflita com conteúdo, convenções ou equilíbrio atuais do Minecraft.
- **Pular** — não trazer. Ou o vanilla moderno/outro mod já cobre, ou quebra o equilíbrio, ou está fora do escopo, ou não vale a complexidade (frequentemente: multiblocos monolíticos).

Quando uma decisão está genuinamente em aberto, marque a linha com **TBD** e abra uma Discussion (Ideas/Polls) — não a deixe padrão silenciosamente.

## Regras de modernização

Quando **Modernizar** é escolhido, prefira estas convenções para que o resultado pareça nativo ao Minecraft moderno:

- **Use materiais vanilla que agora existem.** Cobre é um metal de progressão de primeira classe agora — use-o (já fazemos para cabos e tubo de cobre). Não invente um paralelo a algo que o vanilla adicionou.
- **Corresponda a nomenclatura/itens do vanilla moderno.** Árvores soltam *resina* agora, não "seiva" genérica; mel/favo de mel existem; ametista, fragmentos de eco, etc. são válidos. Alinhe com estes em vez de reintroduzir nomes de itens legados.
- **Reutilize sistemas vanilla onde encaixam.** O bloco **Crafter** vanilla, receitas de fundição (Kiln as reutiliza), tags e data components são alavancagem — prefira-os a reimplementações próprias.
- **Respeite os níveis de progressão modernos.** Pedra → cobre → ferro → ouro → diamante → netherita é a escada vanilla; mapeie níveis nela em vez de no legado (ex.: estanho/bronze ficam como sabor, não como gate obrigatório a menos que ganhe seu lugar). A escada concreta de tiers, as fases de unlock e a convenção "escolha um subconjunto em ordem" ficam em [`progression-tiers.md`](progression-tiers.md) — toda linha com tiers deve derivar dela.
- **Mantenha RF como unidade de energia** via a abstração de energia agnóstica de loader existente (`core.lib`), interoperável com Team Reborn Energy (Fabric) e energia NeoForge.

## Filosofia de equilíbrio

- **Preserve a curva da era 1.7.10.** Duplicação de minérios, throughput de motores, velocidades de máquinas e gating de automação devem *parecer* com o ritmo do pack clássico — grind significativo no início, automação satisfatória no meio, logística abstrata no final.
- **Sem almoços grátis no início.** Conveniências poderosas (logística de rede, autocrafting, mineração em massa) ficam atrás de investimento mecânico/energético anterior.
- **Cada nível deve tornar o grind obsoleto, não a jogabilidade.** Ferramentas avançadas removem o tédio (extração mais rápida, buffers maiores), não os sistemas que os jogadores aprenderam.

## A posição sobre multiblocos

Padrão: **evitar estruturas multiblocos monolíticas.** São tediosas de construir, chatas de depurar e conflitam com o princípio de "colocação simples, conexões visíveis".

Exceções são permitidas quando um multibloco genuinamente ganha seu lugar (ex.: um tanque ou caldeira estilo Railcraft onde *a escala em si é a feature*). Ao portar algo que era um multibloco:

- Prefira um **bloco único** + satélites/marcadores opcionais (como o Laser Quarry já faz com blocos de marcador).
- Se a escala é o ponto, prefira blocos **modulares/ladrilháveis** a um esquema rígido obrigatório.
- Documente a decisão nas notas do detalhamento.

## O template de tabela de decisão

Todo detalhamento em `mods/*.md` usa esta tabela. Copie verbatim:

```markdown
| Feature | What it did (1.7.10–1.12.2) | Decision | Modern take / balance notes | Status | Maps to |
|---|---|---|---|---|---|
```

### Legenda das colunas

- **Feature** — o bloco/item/mecânica concreto do mod de origem.
- **What it did** — descrição de uma linha do comportamento clássico (era 1.7.10–1.12.2).
- **Decision** — `Port` · `Modernize` · `Skip` · `TBD`.
- **Modern take / balance notes** — como realizaríamos (se Port/Modernize) ou por que não (se Skip); considerações de equilíbrio.
- **Status** — estado atual de implementação:
  - ✅ **Done** — implementado e no build.
  - 🚧 **Planned** — aceito, agendado (tem/terá uma issue no Project).
  - **—** Not started — aceito em princípio, ainda não agendado.
  - ❌ **Won't port** — decidido Skip.
- **Maps to** — localização no código (ex.: `power` / engines) ou o épico do roadmap ao qual pertence. Para linhas ✅ Done, deve apontar para um bloco/item/módulo realmente implementado.

### Convenções

- Uma feature por linha; divida features guarda-chuva em sub-linhas se as decisões diferirem.
- Uma linha ✅ Done deve ser verificável contra o código real (`common/src/main/java/com/logistics/`).
- Marque especificações históricas incertas com blockquote: `> TODO: confirmar <coisa>`.
