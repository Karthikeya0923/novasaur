package com.novasaur;

import com.google.mediapipe.tasks.genai.llminference.LlmInference;
import android.content.Context;

public class GemmaInference {

    private LlmInference llmInference;

    private static final String REJECTION =
        "I'm NovaSaur! I only know about dinosaurs and space. Ask me something cool about them!";

    private static final String SYSTEM_PROMPT =
        "You are NovaSaur, a dinosaur and space scientist talking to a curious kid.\n" +
        "Explain things the way a real paleontologist or astronomer would to a child:\n" +
        "accurate, but warm, exciting, and easy to understand.\n" +
        "\n" +
        "How to answer:\n" +
        "- Use the FACTS below. They are true. Build your answer on them.\n" +
        "- Keep it to 1 to 3 short sentences in simple, friendly words.\n" +
        "- Sound genuinely excited about how cool this stuff is.\n" +
        "- Never make things up or guess. If the facts do not cover it, say you are not sure.\n" +
        "- You can add one fun 'wow' detail, but only if it is in the facts.\n";

    // ===== Knowledge base lives right here in this same file =====

    private static class Fact {
        final String text;
        final String[] keys;
        Fact(String text, String... keys) {
            this.text = text;
            this.keys = keys;
        }
    }

    private static final Fact[] FACTS = new Fact[] {

        // ---- Dinosaur species ----
        new Fact("Tyrannosaurus rex was a huge meat-eating dinosaur with powerful jaws and tiny arms. It lived around 68 million years ago and was one of the largest land predators ever.",
            "trex", "t rex", "t. rex", "t-rex", "rex", "tyrannosaurus"),
        new Fact("Velociraptor was a small, fast, feathered dinosaur about the size of a turkey, not the giant from the movies. It hunted with a big curved claw on each foot.",
            "velociraptor", "raptor"),
        new Fact("Triceratops was a plant-eating dinosaur with three horns on its face and a large bony frill. It used its horns to defend itself from predators like T. rex.",
            "triceratops"),
        new Fact("Stegosaurus was a plant-eater with rows of bony plates along its back and sharp spikes on its tail. It swung its spiky tail to fight off attackers.",
            "stegosaurus"),
        new Fact("Brachiosaurus was a giant plant-eating dinosaur with a very long neck, taller than a four-story building. It reached up to eat leaves at the tops of trees.",
            "brachiosaurus"),
        new Fact("Spinosaurus was a huge meat-eater with a tall sail on its back, even longer than T. rex. It spent much of its time in water and ate fish.",
            "spinosaurus"),
        new Fact("Ankylosaurus was a plant-eater covered in thick bony armor with a heavy club on its tail. It swung the club to protect itself from predators.",
            "ankylosaurus"),
        new Fact("Allosaurus was a large meat-eating dinosaur that lived about 150 million years ago, before T. rex. It had sharp teeth and strong claws for hunting.",
            "allosaurus"),
        new Fact("Diplodocus was one of the longest dinosaurs, with a very long neck and an even longer, whip-like tail. It was a gentle plant-eater.",
            "diplodocus"),
        new Fact("Apatosaurus, also known as Brontosaurus, was a massive long-necked plant-eater. It was one of the heaviest animals ever to walk on land.",
            "apatosaurus", "brontosaurus"),
        new Fact("Iguanodon was a plant-eating dinosaur with a sharp thumb spike it may have used for defense. It could walk on two legs or all four.",
            "iguanodon"),
        new Fact("Parasaurolophus was a duck-billed plant-eater with a long, curved crest on its head. Scientists think it used the crest to make loud, trumpet-like sounds.",
            "parasaurolophus"),
        new Fact("Pachycephalosaurus had a thick, bony dome on top of its head. It may have used its hard head for head-butting contests.",
            "pachycephalosaurus", "dome head", "bone head"),
        new Fact("Carnotaurus was a fast meat-eating dinosaur with two horns above its eyes and very tiny arms, even smaller than T. rex's.",
            "carnotaurus"),
        new Fact("Compsognathus was one of the smallest dinosaurs, about the size of a chicken. It was a quick little meat-eater.",
            "compsognathus", "smallest dinosaur"),
        new Fact("Archaeopteryx lived about 150 million years ago and had feathers and wings like a bird, but teeth and a tail like a dinosaur. It shows how birds evolved from dinosaurs.",
            "archaeopteryx", "first bird"),

        // ---- Dinosaurs in general ----
        new Fact("Argentinosaurus was one of the largest dinosaurs ever, a giant long-necked plant-eater as long as a few school buses lined up.",
            "biggest dinosaur", "largest dinosaur", "argentinosaurus", "heaviest dinosaur"),
        new Fact("Some small, lightweight dinosaurs could run very fast, maybe as fast as a racehorse. Big heavy ones like Brachiosaurus moved slowly.",
            "fastest dinosaur", "how fast", "run fast"),
        new Fact("Many dinosaurs, especially the meat-eaters, had feathers. Some of them looked quite a lot like big birds.",
            "feather", "feathers", "have feathers"),
        new Fact("Dinosaurs laid eggs, often in nests, much like birds and reptiles do today. Some dinosaurs even guarded their nests.",
            "egg", "eggs", "nest", "baby dinosaur", "hatch"),
        new Fact("Scientists think many dinosaurs were warm-blooded and active, more like birds than like modern cold-blooded reptiles.",
            "warm blood", "cold blood", "warm-blooded", "cold-blooded", "blooded"),
        new Fact("Some dinosaurs ate only plants (herbivores) and some ate meat (carnivores). A few ate both.",
            "herbivore", "carnivore", "what did dinosaurs eat", "meat eater", "plant eater"),
        new Fact("About 66 million years ago, a giant asteroid crashed into Earth. The impact changed the climate and wiped out most of the dinosaurs.",
            "killed the dinosaur", "kill the dinosaur", "die out", "died out", "wiped out", "extinct", "extinction", "happened to the dinosaur", "asteroid"),
        new Fact("Birds are actually living dinosaurs! They evolved from small feathered dinosaurs, which means dinosaurs never completely disappeared.",
            "bird", "birds", "still alive", "alive today", "living dinosaur", "all the dinosaur", "all dinosaur", "any dinosaur left"),
        new Fact("Dinosaurs lived during the Mesozoic Era, from about 250 million to 66 million years ago. That was many millions of years before any humans existed.",
            "when did the dinosaur", "when did dinosaur", "how long ago", "mesozoic", "jurassic", "cretaceous", "triassic", "what period"),
        new Fact("No, humans and dinosaurs never lived at the same time. Dinosaurs died out about 66 million years ago, and humans appeared only a few hundred thousand years ago.",
            "human", "people", "caveman", "same time as dinosaur", "with dinosaur", "live with dinosaur", "person and dinosaur"),
        new Fact("A fossil is the preserved remains or imprint of a living thing from long ago, slowly turned to stone over millions of years. Scientists called paleontologists dig them up and study them.",
            "fossil", "paleontolog", "dig up", "how do we know about dinosaur"),

        // ---- Prehistoric creatures that were NOT dinosaurs ----
        new Fact("Pteranodon was a flying reptile with huge wings, but it was not actually a dinosaur. It soared over the ocean and snatched up fish.",
            "pteranodon", "pterodactyl", "pterosaur", "flying dinosaur"),
        new Fact("Mosasaurus was a giant sea reptile that lived in the oceans during the dinosaur age, but it was not a dinosaur itself.",
            "mosasaurus"),
        new Fact("Plesiosaurus was a sea reptile with a long neck and flippers. It lived alongside the dinosaurs but was not one of them.",
            "plesiosaurus", "loch ness"),
        new Fact("Megalodon was a giant prehistoric shark, not a dinosaur, and it lived millions of years after the dinosaurs were already gone.",
            "megalodon"),
        new Fact("Dimetrodon had a big sail on its back, but it was not a dinosaur. It lived even earlier, before the dinosaurs appeared.",
            "dimetrodon"),

        // ---- The Sun and planets ----
        new Fact("The Sun is a giant ball of hot glowing gas, and it is actually a star. Its surface is about 5,500 degrees Celsius, and it gives Earth light and heat.",
            "sun"),
        new Fact("Mercury is the closest planet to the Sun and the smallest planet. It has no moons and is covered in craters.",
            "mercury"),
        new Fact("Venus is the hottest planet because its thick clouds trap heat like a blanket. It is almost the same size as Earth.",
            "venus"),
        new Fact("Earth is our home planet and the only place we know of with life. It has one moon and is mostly covered in water.",
            "earth"),
        new Fact("Mars is called the Red Planet because of its rusty red dust. It has two small moons named Phobos and Deimos, and robot rovers have explored it.",
            "mars", "phobos", "deimos"),
        new Fact("Jupiter is the largest planet in the solar system, so big that all the other planets could fit inside it. It has a giant storm called the Great Red Spot and at least 95 known moons.",
            "jupiter", "great red spot"),
        new Fact("Saturn is famous for its beautiful rings made of ice and rock. It is the second-largest planet and has more than 140 moons.",
            "saturn", "rings"),
        new Fact("Uranus is a cold, blue-green planet that is tipped over and spins on its side. It has faint rings and many moons.",
            "uranus"),
        new Fact("Neptune is the farthest planet from the Sun and is a deep blue color. It is extremely cold and has the fastest winds in the solar system.",
            "neptune"),
        new Fact("Pluto used to be called the ninth planet, but in 2006 scientists reclassified it as a dwarf planet, not one of the eight main planets. It is small and very far away.",
            "pluto", "dwarf planet", "ninth planet"),
        new Fact("Our solar system has eight planets that orbit the Sun: Mercury, Venus, Earth, Mars, Jupiter, Saturn, Uranus, and Neptune.",
            "how many planet", "solar system", "list of planet", "all the planet", "eight planet", "nine planet"),

        // ---- Moons, deep space, and phenomena ----
        new Fact("The Moon is Earth's only natural satellite, about 384,000 kilometers away. It has no air, and astronauts have walked on it.",
            "the moon", "moon is", "how far is the moon", "about the moon", "walk on the moon", "earth's moon"),
        new Fact("The Moon looks like it changes shape because, as it orbits Earth, we see different amounts of its sunlit side. These shapes are called the phases of the Moon.",
            "moon phase", "phases", "moon change", "why does the moon", "crescent", "full moon"),
        new Fact("A black hole is a place in space where gravity is so strong that not even light can escape. Black holes form when very large stars collapse.",
            "black hole", "blackhole"),
        new Fact("Stars are giant balls of hot gas that make their own light, just like our Sun. They look tiny only because they are incredibly far away.",
            "star", "stars", "constellation"),
        new Fact("A galaxy is a huge group of billions of stars held together by gravity. We live in a galaxy called the Milky Way.",
            "galaxy", "galaxies", "milky way", "milkyway"),
        new Fact("Gravity is the invisible force that pulls objects toward each other. Earth's gravity keeps us on the ground and keeps the Moon orbiting our planet.",
            "gravity"),
        new Fact("An eclipse happens when one space object moves into the shadow of another. A solar eclipse is when the Moon blocks the Sun; a lunar eclipse is when Earth's shadow falls on the Moon.",
            "eclipse"),
        new Fact("A comet is a ball of ice and dust that grows a glowing tail near the Sun. An asteroid is a rocky object in space, and a meteor is a space rock that burns up in our sky as a shooting star.",
            "comet", "asteroid belt", "meteor", "shooting star", "meteorite"),
        new Fact("A light-year is the distance light travels in one whole year. Space is so huge that scientists measure it in light-years instead of kilometers.",
            "light year", "light-year", "lightyear"),
        new Fact("Scientists think the universe began about 13.8 billion years ago in an event called the Big Bang, and it has been growing bigger ever since.",
            "universe", "big bang", "how old is space", "start of space"),

        // ---- Space exploration ----
        new Fact("An astronaut is a person trained to live and work in space. The first person to walk on the Moon was Neil Armstrong, in 1969.",
            "astronaut", "first person on the moon", "first man on the moon", "who walked on the moon", "neil armstrong"),
        new Fact("No human has traveled to Mars yet. So far only robot rovers and spacecraft have explored it, but scientists hope people can go there one day.",
            "walk on mars", "walked on mars", "been to mars", "go to mars", "people on mars", "humans on mars", "anyone on mars", "land on mars"),
        new Fact("The first human to travel into space was Yuri Gagarin in 1961, and the first satellite, Sputnik, was launched in 1957.",
            "first in space", "first human in space", "first person in space", "gagarin", "sputnik", "first satellite"),
        new Fact("The International Space Station is a large laboratory that orbits Earth, where astronauts from many countries live and do science experiments.",
            "space station", "iss", "international space"),
        new Fact("A rocket is a vehicle that burns fuel to launch itself into space, fast enough to escape Earth's gravity. Rockets carry astronauts and satellites up to space.",
            "rocket"),
        new Fact("Telescopes let us see far into space. Powerful space telescopes like Hubble and James Webb have taken amazing pictures of distant stars and galaxies.",
            "telescope", "hubble", "james webb"),
        new Fact("Astronomy is the science of studying space, including planets, stars, and galaxies. People who study it are called astronomers.",
            "astronomy", "astronomer"),
    };

    private static final String[] ALLOWED = new String[] {
        "dinosaur", "dino", "trex", "t rex", "t. rex", "t-rex", "rex", "tyrannosaurus",
        "velociraptor", "raptor", "triceratops", "stegosaurus", "brachiosaurus",
        "spinosaurus", "ankylosaurus", "allosaurus", "diplodocus", "apatosaurus",
        "brontosaurus", "iguanodon", "parasaurolophus", "pachycephalosaurus",
        "carnotaurus", "compsognathus", "archaeopteryx", "argentinosaurus",
        "pteranodon", "pterodactyl", "pterosaur", "mosasaurus", "plesiosaurus",
        "megalodon", "dimetrodon", "theropod", "sauropod", "jurassic", "cretaceous",
        "triassic", "fossil", "extinct", "extinction", "prehistoric", "paleontolog",
        "mesozoic", "herbivore", "carnivore", "reptile", "feather", "dome head", "bone head",
        "space", "planet", "moon", "sun", "solar", "star", "constellation", "galaxy",
        "galaxies", "universe", "cosmos", "rocket", "spaceship", "astronaut", "astronomy",
        "astronomer", "mars", "phobos", "deimos", "earth", "jupiter", "saturn", "uranus",
        "neptune", "mercury", "venus", "pluto", "comet", "asteroid", "meteor", "meteorite",
        "nebula", "black hole", "blackhole", "orbit", "gravity", "eclipse", "milky way",
        "milkyway", "nasa", "apollo", "telescope", "hubble", "james webb", "satellite",
        "big bang", "light year", "light-year", "lightyear", "gagarin", "sputnik",
        "space station", "iss", "rings", "great red spot", "dwarf planet"
    };

    private static String lookupFacts(String question) {
        String q = question.toLowerCase();
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Fact f : FACTS) {
            boolean matched = false;
            for (String k : f.keys) {
                if (q.contains(k)) {
                    matched = true;
                    break;
                }
            }
            if (matched) {
                sb.append("- ").append(f.text).append("\n");
                count++;
                if (count >= 3) {
                    break;
                }
            }
        }
        return sb.toString();
    }

    private static boolean isOnTopic(String question) {
        String q = question.toLowerCase();
        for (String word : ALLOWED) {
            if (q.contains(word)) {
                return true;
            }
        }
        return false;
    }

    // ===== Inference =====

    public interface ResponseCallback {
        void onPartialResponse(String partial);
        void onComplete(String fullResponse);
        void onError(String error);
    }

    public GemmaInference(Context context, String modelPath) {
        LlmInference.LlmInferenceOptions options =
            LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)
                .setMaxTokens(512)
                .setResultListener((partialResult, done) -> {})
                .build();
        llmInference = LlmInference.createFromOptions(context, options);
    }

    private String friendlyReply(String question) {
        String q = question.toLowerCase().trim();

        if (q.equals("hi") || q.equals("hello") || q.equals("hey") ||
            q.startsWith("hi ") || q.startsWith("hello ") || q.startsWith("hey ")) {
            return "Hi there! I'm NovaSaur. Ask me anything about dinosaurs or space!";
        }
        if (q.contains("your name") || q.contains("who are you")) {
            return "My name is NovaSaur, your dinosaur and space buddy! Want to hear a cool fact?";
        }
        if (q.contains("are you real") || q.contains("are you alive") ||
            q.contains("are you a robot") || q.contains("are you ai") ||
            q.contains("are you human")) {
            return "I'm NovaSaur, a friendly helper here in the app! I love talking about dinosaurs and space. What do you want to know?";
        }
        if (q.contains("who made you") || q.contains("who created you")) {
            return "I'm NovaSaur, an AI helper built into this app to share awesome dinosaur and space facts with you!";
        }
        if (q.contains("how are you")) {
            return "I'm great, thanks for asking! I'm always excited to talk about dinosaurs and space. Ask me something!";
        }
        if (q.contains("thank you") || q.equals("thanks") || q.startsWith("thanks ")) {
            return "You're welcome! Want to learn another cool fact about dinosaurs or space?";
        }
        if (q.contains("i love you") || q.contains("you're cool") || q.contains("you are cool") ||
            q.contains("you're awesome") || q.contains("you are awesome")) {
            return "Aw, thanks! I love sharing dinosaur and space facts with you. Ask me anything!";
        }
        if (q.contains("are you a dinosaur")) {
            return "Ha! I'm NovaSaur, your dino and space guide. Want to hear about a real dinosaur?";
        }
        if (q.equals("bye") || q.startsWith("bye") || q.contains("goodbye")) {
            return "Bye for now! Come back soon for more dinosaur and space facts!";
        }
        return null;
    }

    private String buildPrompt(String facts, String question) {
        String factBlock = facts.isEmpty()
            ? ""
            : "Here are true facts you can use:\n" + facts + "\n";

        return
            "<start_of_turn>user\n" +
            SYSTEM_PROMPT + "\n" +
            factBlock +
            "Question: " + question + "\n" +
            "<end_of_turn>\n" +
            "<start_of_turn>model\n";
    }

    public String ask(String userQuestion) {

        String friendly = friendlyReply(userQuestion);
        if (friendly != null) {
            return friendly;
        }

        if (!isOnTopic(userQuestion)) {
            return REJECTION;
        }

        String facts = lookupFacts(userQuestion);
        String fullPrompt = buildPrompt(facts, userQuestion);

        String response = llmInference.generateResponse(fullPrompt);

        if (response == null || response.trim().isEmpty()) {
            return "Hmm, I'm not totally sure about that one. Try asking me another dinosaur or space question!";
        }

        return response.trim();
    }

    public void askAsync(String userQuestion, ResponseCallback callback) {
        try {
            callback.onComplete(ask(userQuestion));
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    public void close() {
        if (llmInference != null) {
            llmInference.close();
        }
    }
}