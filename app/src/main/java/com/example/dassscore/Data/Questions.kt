package com.example.dassscore.Data

import com.example.dassscore.DassCategory
import com.example.dassscore.DassQuestion

class Questions (){
    val questions = listOf(
        // Depression Questions (14 questions)
        DassQuestion(0,"I couldn't seem to experience any positive feeling at all", DassCategory.DEPRESSION),
        DassQuestion(1, "I found it difficult to work up the initiative to do things", DassCategory.DEPRESSION),
        DassQuestion(2, "I felt that I had nothing to look forward to", DassCategory.DEPRESSION),
        DassQuestion(3, "I felt down-hearted and blue", DassCategory.DEPRESSION),
        DassQuestion(4, "I was unable to become enthusiastic about anything", DassCategory.DEPRESSION),
        DassQuestion(5, "I felt I wasn't worth much as a person", DassCategory.DEPRESSION),
        DassQuestion(6, "I felt that life was meaningless", DassCategory.DEPRESSION),
        DassQuestion(7, "I found it hard to have energy for things", DassCategory.DEPRESSION),
        DassQuestion(8, "I felt sad and depressed", DassCategory.DEPRESSION),
        DassQuestion(9, "I found myself getting upset by quite trivial things", DassCategory.DEPRESSION),
        DassQuestion(10, "I felt that I had lost interest in just about everything", DassCategory.DEPRESSION),
        DassQuestion(11, "I felt I was pretty worthless", DassCategory.DEPRESSION),
        DassQuestion(12, "I could see nothing in the future to be hopeful about", DassCategory.DEPRESSION),
        DassQuestion(13, "I felt that life wasn't worthwhile", DassCategory.DEPRESSION),

        // Anxiety Questions (14 questions)
        DassQuestion(14, "I was aware of dryness of my mouth", DassCategory.ANXIETY),
        DassQuestion(15, "I experienced breathing difficulty", DassCategory.ANXIETY),
        DassQuestion(16, "I experienced trembling (eg, in the hands)", DassCategory.ANXIETY),
        DassQuestion(17, "I was worried about situations in which I might panic and make a fool of myself", DassCategory.ANXIETY),
        DassQuestion(18, "I felt I was close to panic", DassCategory.ANXIETY),
        DassQuestion(19, "I was aware of the action of my heart in the absence of physical exertion", DassCategory.ANXIETY),
        DassQuestion(20, "I felt scared without any good reason", DassCategory.ANXIETY),
        DassQuestion(21, "I had a feeling of shakiness (eg, legs going to give way)", DassCategory.ANXIETY),
        DassQuestion(22, "I found myself in situations that made me so anxious I was most relieved when they ended", DassCategory.ANXIETY),
        DassQuestion(23, "I had a feeling of faintness", DassCategory.ANXIETY),
        DassQuestion(24, "I perspired noticeably (eg, hands sweaty) in the absence of high temperatures or physical exertion", DassCategory.ANXIETY),
        DassQuestion(25, "I felt terrified", DassCategory.ANXIETY),
        DassQuestion(26, "I was worried about situations in which I might panic", DassCategory.ANXIETY),
        DassQuestion(27, "I experienced sudden feelings of panic", DassCategory.ANXIETY),

        // Stress Questions (14 questions)
        DassQuestion(28, "I found it hard to wind down", DassCategory.STRESS),
        DassQuestion(29, "I tended to over-react to situations", DassCategory.STRESS),
        DassQuestion(30, "I felt that I was using a lot of nervous energy", DassCategory.STRESS),
        DassQuestion(31, "I found myself getting agitated", DassCategory.STRESS),
        DassQuestion(32, "I found it difficult to relax", DassCategory.STRESS),
        DassQuestion(33, "I was intolerant of anything that kept me from getting on with what I was doing", DassCategory.STRESS),
        DassQuestion(34, "I felt that I was rather touchy", DassCategory.STRESS),
        DassQuestion(35, "I found myself getting upset rather easily", DassCategory.STRESS),
        DassQuestion(36, "I felt that I was getting worked up", DassCategory.STRESS),
        DassQuestion(37, "I found it hard to calm down after something upset me", DassCategory.STRESS),
        DassQuestion(38, "I found it difficult to tolerate interruptions to what I was doing", DassCategory.STRESS),
        DassQuestion(39, "I was in a state of nervous tension", DassCategory.STRESS),
        DassQuestion(40, "I found myself getting impatient when I was delayed in any way", DassCategory.STRESS),
        DassQuestion(41, "I felt that I was rather sensitive", DassCategory.STRESS)
    )
}