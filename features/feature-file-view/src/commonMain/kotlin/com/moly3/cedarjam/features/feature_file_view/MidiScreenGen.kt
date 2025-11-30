package com.moly3.cedarjam.features.feature_file_view

object MidiScreenGen {

    fun genHtml(key: String?, chord: List<Int>): String {
        val html2 = """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <title>MIDI → Piano Sheet</title>
    <script src="https://cdn.jsdelivr.net/npm/vexflow@5.0.0/build/cjs/vexflow.js"></script>
    <style>
        body { fonts-family: sans-serif; margin: 20px; }
        #notation { border: 1px solid #ccc; }
        .controls { margin-bottom: 20px; }
        button { margin: 5px; padding: 8px 16px; }
        .chord-info { margin: 10px 0; fonts-family: monospace; }
    </style>
</head>
<body>

<!--<h3>MIDI → Piano Sheet with Dynamic Note Distribution</h3>-->

<!--<div class="controls">-->
<!--    <div>-->
<!--        <strong>Key Signature:</strong>-->
<!--        <button onclick="currentKey = null; testChord(currentChord)">C Major (No Key)</button>-->
<!--        <button onclick="currentKey = 'F'; testChord(currentChord)">F Major (1♭)</button>-->
<!--        <button onclick="currentKey = 'Dm'; testChord(currentChord)">D Minor (1♭)</button>-->
<!--        <button onclick="currentKey = 'Bb'; testChord(currentChord)">B♭ Major (2♭)</button>-->
<!--        <button onclick="currentKey = 'Gm'; testChord(currentChord)">G Minor (2♭)</button>-->
<!--        <button onclick="currentKey = 'Eb'; testChord(currentChord)">E♭ Major (3♭)</button>-->
<!--        <button onclick="currentKey = 'Cm'; testChord(currentChord)">C Minor (3♭)</button>-->
<!--        <button onclick="currentKey = 'Ab'; testChord(currentChord)">A♭ Major (4♭)</button>-->
<!--        <button onclick="currentKey = 'Fm'; testChord(currentChord)">F Minor (4♭)</button>-->
<!--        <button onclick="currentKey = 'Db'; testChord(currentChord)">D♭ Major (5♭)</button>-->
<!--        <button onclick="currentKey = 'Bbm'; testChord(currentChord)">B♭ Minor (5♭)</button>-->
<!--    </div>-->
<!--    <br>-->
<!--    <div>-->
<!--        <strong>Sharp Keys:</strong>-->
<!--        <button onclick="currentKey = 'G'; testChord(currentChord)">G Major (1♯)</button>-->
<!--        <button onclick="currentKey = 'Em'; testChord(currentChord)">E Minor (1♯)</button>-->
<!--        <button onclick="currentKey = 'D'; testChord(currentChord)">D Major (2♯)</button>-->
<!--        <button onclick="currentKey = 'Bm'; testChord(currentChord)">B Minor (2♯)</button>-->
<!--        <button onclick="currentKey = 'A'; testChord(currentChord)">A Major (3♯)</button>-->
<!--        <button onclick="currentKey = 'F#m'; testChord(currentChord)">F♯ Minor (3♯)</button>-->
<!--        <button onclick="currentKey = 'E'; testChord(currentChord)">E Major (4♯)</button>-->
<!--        <button onclick="currentKey = 'C#m'; testChord(currentChord)">C♯ Minor (4♯)</button>-->
<!--    </div>-->
<!--    <br>-->
<!--    <div>-->
<!--        <strong>Test Chords:</strong>-->
<!--        <button onclick="testChord([55, 59, 62, 71])">Test Chord 1 (G3, B3, D4, B4)</button>-->
<!--        <button onclick="testChord([48, 52, 55, 59, 64, 67])">Test Chord 2 (C3-G4)</button>-->
<!--        <button onclick="testChord([60, 64, 67, 72, 76])">Test Chord 3 (C4-E5)</button>-->
<!--        <button onclick="testChord([36, 40, 43, 48, 60])">Test Chord 4 (C2-C4)</button>-->
<!--    </div>-->
<!--</div>-->

<!--<div class="chord-info" id="chordInfo"></div>-->
<div id="notation"></div>

<script>
    const VF = VexFlow;

    // Key signature definitions
    const keySignatures = {
        // Flat keys
        'F': { flats: ['b'], sharps: [] },
        'Dm': { flats: ['b'], sharps: [] },
        'Bb': { flats: ['b', 'e'], sharps: [] },
        'Gm': { flats: ['b', 'e'], sharps: [] },
        'Eb': { flats: ['b', 'e', 'a'], sharps: [] },
        'Cm': { flats: ['b', 'e', 'a'], sharps: [] },
        'Ab': { flats: ['b', 'e', 'a', 'd'], sharps: [] },
        'Fm': { flats: ['b', 'e', 'a', 'd'], sharps: [] },
        'Db': { flats: ['b', 'e', 'a', 'd', 'g'], sharps: [] },
        'Bbm': { flats: ['b', 'e', 'a', 'd', 'g'], sharps: [] },

        // Sharp keys
        'G': { flats: [], sharps: ['f'] },
        'Em': { flats: [], sharps: ['f'] },
        'D': { flats: [], sharps: ['f', 'c'] },
        'Bm': { flats: [], sharps: ['f', 'c'] },
        'A': { flats: [], sharps: ['f', 'c', 'g'] },
        'F#m': { flats: [], sharps: ['f', 'c', 'g'] },
        'E': { flats: [], sharps: ['f', 'c', 'g', 'd'] },
        'C#m': { flats: [], sharps: ['f', 'c', 'g', 'd'] }
    };

    function midiToNoteName(midi, keySignature = null) {
        const noteNames = ["c", "c#", "d", "d#", "e", "f", "f#", "g", "g#", "a", "a#", "b"];
        const flatNames = ["c", "db", "d", "eb", "e", "f", "gb", "g", "ab", "a", "bb", "b"];

        const noteIndex = midi % 12;
        const octave = Math.floor(midi / 12) - 1;

        // If no key signature, use sharps by default
        if (!keySignature || !keySignatures[keySignature]) {
            return `${'$'}{noteNames[noteIndex]}/${'$'}{octave}`;
        }

        const keyInfo = keySignatures[keySignature];

        // Use flat names for flat keys, sharp names for sharp keys
        if (keyInfo.flats.length > 0) {
            return `${'$'}{flatNames[noteIndex]}/${'$'}{octave}`;
        } else {
            return `${'$'}{noteNames[noteIndex]}/${'$'}{octave}`;
        }
    }

    function distributeNotes(midiChord) {
        // Средняя до (C4 = MIDI 60) как граница между ключами
        const MIDDLE_C = 60;

        const sorted = [...midiChord].sort((a, b) => a - b);

        // Разделяем на два стана по средней до
        const trebleNotes = sorted.filter(midi => midi >= MIDDLE_C);
        const bassNotes = sorted.filter(midi => midi < MIDDLE_C);

        return { trebleNotes, bassNotes };
    }

    function createStaveNote(clef, midiNotes, keySignature = null, duration = "q") {
        if (midiNotes.length === 0) return null;

        const keys = midiNotes.map(midi => midiToNoteName(midi, keySignature));
        const note = new VF.StaveNote({
            clef: clef,
            keys: keys,
            duration: duration
        });

        // Add accidentals only if they're not in the key signature
        const keyInfo = keySignature ? keySignatures[keySignature] : null;

        keys.forEach((key, i) => {
            const noteName = key.split('/')[0];
            const baseNote = noteName.replace(/[#b]/g, '');

            if (key.includes("#")) {
                // Only add sharp if it's not in the key signature
                if (!keyInfo || !keyInfo.sharps.includes(baseNote)) {
                    note.addModifier(new VF.Accidental("#"), i);
                }
            } else if (key.includes("b")) {
                // Only add flat if it's not in the key signature
                if (!keyInfo || !keyInfo.flats.includes(baseNote)) {
                    note.addModifier(new VF.Accidental("b"), i);
                }
            }
        });

        return note;
    }

    function drawNotation(midiChord, keySignature = null) {
        const div = document.getElementById("notation");
        div.innerHTML = ""; // Очищаем предыдущую нотацию

        const renderer = new VF.Renderer(div, VF.Renderer.Backends.SVG);
        renderer.resize(700, 300);
        const context = renderer.getContext();

        // Скрипичный стан
        const trebleStave = new VF.Stave(50, 20, 600);
        trebleStave.addClef("treble");
        if (keySignature) {
            trebleStave.addKeySignature(keySignature);
        }
        trebleStave.addTimeSignature("4/4");
        trebleStave.setContext(context).draw();

        // Басовый стан
        const bassStave = new VF.Stave(50, 150, 600);
        bassStave.addClef("bass");
        if (keySignature) {
            bassStave.addKeySignature(keySignature);
        }
        bassStave.addTimeSignature("4/4");
        bassStave.setContext(context).draw();

        // Скобки фортепиано
        new VF.StaveConnector(trebleStave, bassStave).setType(VF.StaveConnector.type.BRACE).setContext(context).draw();
        new VF.StaveConnector(trebleStave, bassStave).setType(VF.StaveConnector.type.SINGLE_LEFT).setContext(context).draw();
        new VF.StaveConnector(trebleStave, bassStave).setType(VF.StaveConnector.type.SINGLE_RIGHT).setContext(context).draw();

        // Распределяем ноты
        const { trebleNotes, bassNotes } = distributeNotes(midiChord);

        // Создаем ноты
        const trebleNote = createStaveNote("treble", trebleNotes, keySignature);
        const bassNote = createStaveNote("bass", bassNotes, keySignature);

        // Создаем паузы для пустых станов
        const trebleRest = trebleNotes.length === 0 ? new VF.StaveNote({
            clef: "treble",
            keys: ["d/5"],
            duration: "qr"
        }) : null;

        const bassRest = bassNotes.length === 0 ? new VF.StaveNote({
            clef: "bass",
            keys: ["d/3"],
            duration: "qr"
        }) : null;

        // Отрисовываем
        if (trebleNote) {
            VF.Formatter.FormatAndDraw(context, trebleStave, [trebleNote]);
        } else if (trebleRest) {
            VF.Formatter.FormatAndDraw(context, trebleStave, [trebleRest]);
        }

        if (bassNote) {
            VF.Formatter.FormatAndDraw(context, bassStave, [bassNote]);
        } else if (bassRest) {
            VF.Formatter.FormatAndDraw(context, bassStave, [bassRest]);
        }

        // Показываем информацию о распределении
        const chordInfo = document.getElementById("chordInfo");
        const trebleNoteNames = trebleNotes.map(midi => midiToNoteName(midi, keySignature));
        const bassNoteNames = bassNotes.map(midi => midiToNoteName(midi, keySignature));

//        chordInfo.innerHTML = `
//            <strong>Key Signature:</strong> ${'$'}{keySignature || 'C Major (no accidentals)'}<br>
//            <strong>MIDI Notes:</strong> [${'$'}{midiChord.join(', ')}]<br>
//            <strong>Treble (≥C4):</strong> [${'$'}{trebleNotes.join(', ')}] → ${'$'}{trebleNoteNames.join(', ')}<br>
//            <strong>Bass (<C4):</strong> [${'$'}{bassNotes.join(', ')}] → ${'$'}{bassNoteNames.join(', ')}
//        `;
    }

    function testChord(midiChord) {
        currentChord = midiChord;
        drawNotation(midiChord, currentKey);
    }

    // Global variables to track current state
    let currentKey = null;
    let currentChord = [60];

    // Инициализация с первым примером
    testChord(currentChord);
    
    const refreshNotes = (currentKey1, currentChord1) => {
        currentKey = currentKey1;
        //currentChord = currentChord1;
        //currentKey = 'Db';
        //currentChord = [100];
        testChord(currentChord1);
    };
</script>

</body>
</html>

                        """.trimIndent()

        //${'$'}
//        var html3 = html2
//            .replace("{current_key}", if(key==null) "null" else "'$key'")
//            .replace("{current_chord}", chord.joinToString(","))
        return html2
    }
}