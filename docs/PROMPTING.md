# Prompting a small on-device model

Big hosted models forgive sloppy prompts. A 4-bit quantized model on a phone CPU does not. These are the rules NovaSaur's production integration (DinoSpace) settled on, and the prompt template it ships.

## The golden rule: don't call the model

The fastest, most accurate token is the one you never generate. DinoSpace routes every question through a local answer layer first:

1. **Entity questions** ("how strong is a T. Rex?") — answered directly from the encyclopedia. Instant, always correct.
2. **Curated topics** ("why did the dinosaurs go extinct?") — answered from a keyword-matched knowledge base of hand-written facts.
3. **Live data** ("is the moon full tonight?") — answered by an astronomy engine. A frozen model can never know this; the app can.
4. Only what's left — genuinely open-ended questions — reaches the model.

In production this means ~95%+ of real questions answer in under a second without inference.

## Retrieval that fits in a phone-sized prompt

Prompt length is the main driver of "thinking…" time on-device: every token of prompt costs real milliseconds of prefill. The retrieval layer therefore:

- matches entities with typo tolerance (bounded edit distance), aliases ("t rex", "trex"), and kid abbreviations ("brachio")
- resolves follow-up pronouns ("how fast was **it**?") against the last-mentioned entities
- caps the retrieved NOTES block at ~700 characters — one entity summary plus at most one knowledge nugget

## The template

```
You are NovaSaur, a friendly dinosaur and space expert for kids. Answer in
2 to 3 short, clear, accurate sentences a 10-year-old understands. No
emojis, no lists. Trust these facts and copy their exact numbers:
[NAME] <one compact entity summary with stats>
• <one knowledge nugget, if one matched>
The chat so far:
Q: <previous question, snipped>
A: <previous answer, snipped>
Answer the next question in the context of that chat.
Q: <the question>
A:
```

Why it looks like this:

- **Persona + format in one line.** Small models drift; one tight instruction beats three paragraphs.
- **"Copy their exact numbers."** Without it, quantized models round, unit-swap, or invent. With facts in the prompt and this instruction, numeric accuracy is near-perfect.
- **History is two snipped Q/A pairs, not the whole chat.** Enough for follow-ups, cheap on prefill.
- **`A:` primes the completion** so the model doesn't waste tokens on preamble.

## Cleaning the output

Small models leak artifacts. The integration strips, in order: wrapper objects (`Message(text=…)`), special tokens (`<end_of_turn>` etc.), self-continuation (`Q:` a second question it asked itself), markdown, then clamps to 5 sentences / 640 chars, cutting at a sentence boundary.

## Testing without a phone

Everything above the native call — retrieval, grounding, local answers, prompt assembly — is plain C#. DinoSpace runs a desktop harness that pushes dozens of real user questions through the exact production pipeline and asserts each one resolves instantly or composes a valid prompt. If you integrate NovaSaur, steal this idea: your answer pipeline is testable even though the model isn't.
