/* =========================================================
   LITERARY EMPORIUM - SMART FAQ CHATBOT
   FAQ + VOICE INPUT + VOICE OUTPUT
   ========================================================= */

(function () {

    "use strict";

    /* =====================================================
       CREATE CHATBOT UI
       ===================================================== */

    function createChatbot() {

        if (document.getElementById("leChatbot")) {
            return;
        }

        const chatbot = document.createElement("div");

        chatbot.id = "leChatbot";

        chatbot.innerHTML = `
            <style>
                #leChatbot {
                    position: fixed;
                    right: 22px;
                    bottom: 22px;
                    width: 340px;
                    background: #ffffff;
                    border: 1px solid #d9dee5;
                    box-shadow: 0 10px 30px rgba(0,0,0,0.14);
                    font-family: Arial, Helvetica, sans-serif;
                    z-index: 99999;
                    overflow: hidden;
                }

                #leChatHeader {
                    background: #172337;
                    color: #ffffff;
                    padding: 15px 18px;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }

                #leChatHeader strong {
                    font-size: 15px;
                    font-weight: 600;
                }

                #leChatClose {
                    background: none;
                    border: none;
                    color: white;
                    font-size: 20px;
                    cursor: pointer;
                }

                #leChatMessages {
                    height: 310px;
                    padding: 14px;
                    overflow-y: auto;
                    background: #f7f8fa;
                }

                .le-message {
                    margin-bottom: 12px;
                    display: flex;
                }

                .le-message.user {
                    justify-content: flex-end;
                }

                .le-message.bot {
                    justify-content: flex-start;
                }

                .le-bubble {
                    max-width: 82%;
                    padding: 10px 12px;
                    font-size: 13px;
                    line-height: 1.5;
                    border-radius: 4px;
                }

                .le-message.user .le-bubble {
                    background: #172337;
                    color: #ffffff;
                }

                .le-message.bot .le-bubble {
                    background: #ffffff;
                    border: 1px solid #dce1e7;
                    color: #263241;
                }

                #leQuickQuestions {
                    display: flex;
                    gap: 6px;
                    flex-wrap: wrap;
                    padding: 10px;
                    border-top: 1px solid #e0e4e9;
                    background: #ffffff;
                }

                .le-question {
                    border: 1px solid #bfc7d1;
                    background: #ffffff;
                    color: #172337;
                    padding: 6px 9px;
                    font-size: 11px;
                    cursor: pointer;
                }

                .le-question:hover {
                    background: #eef1f4;
                }

                #leChatInputArea {
                    display: flex;
                    gap: 6px;
                    padding: 10px;
                    border-top: 1px solid #dce1e7;
                    background: #ffffff;
                }

                #leChatInput {
                    flex: 1;
                    padding: 10px;
                    border: 1px solid #cbd2da;
                    outline: none;
                    font-size: 13px;
                }

                #leChatInput:focus {
                    border-color: #172337;
                }

                .le-action-button {
                    border: none;
                    background: #172337;
                    color: #ffffff;
                    padding: 9px 11px;
                    cursor: pointer;
                    font-size: 12px;
                }

                .le-action-button:hover {
                    background: #27364c;
                }

                #leVoiceStatus {
                    padding: 0 10px 8px;
                    font-size: 11px;
                    color: #687384;
                    background: #ffffff;
                }

                #leChatOpenButton {
                    position: fixed;
                    right: 22px;
                    bottom: 22px;
                    width: 58px;
                    height: 58px;
                    border: none;
                    background: #172337;
                    color: white;
                    cursor: pointer;
                    font-size: 12px;
                    z-index: 99998;
                }

                @media (max-width: 500px) {

                    #leChatbot {
                        right: 10px;
                        left: 10px;
                        width: auto;
                        bottom: 10px;
                    }

                    #leChatOpenButton {
                        right: 15px;
                        bottom: 15px;
                    }
                }
            </style>

            <button id="leChatOpenButton" type="button">
                Chat
            </button>

            <div id="leChatPanel" style="display:none;">

                <div id="leChatHeader">

                    <strong>
                        Literary Emporium Assistant
                    </strong>

                    <button
                        id="leChatClose"
                        type="button">
                        ×
                    </button>

                </div>

                <div id="leChatMessages"></div>

                <div id="leQuickQuestions"></div>

                <div id="leVoiceStatus">
                    Voice ready
                </div>

                <div id="leChatInputArea">

                    <input
                        id="leChatInput"
                        type="text"
                        placeholder="Ask about books, cart, orders..."
                    >

                    <button
                        id="leMicButton"
                        class="le-action-button"
                        type="button">
                        Mic
                    </button>

                    <button
                        id="leSendButton"
                        class="le-action-button"
                        type="button">
                        Send
                    </button>

                </div>

            </div>
        `;

        document.body.appendChild(chatbot);
    }


    /* =====================================================
       ADD MESSAGE
       ===================================================== */

    function addMessage(text, sender) {

        const messages =
            document.getElementById("leChatMessages");

        if (!messages) {
            return;
        }

        const wrapper =
            document.createElement("div");

        wrapper.className =
            "le-message " + sender;

        const bubble =
            document.createElement("div");

        bubble.className =
            "le-bubble";

        bubble.textContent = text;

        wrapper.appendChild(bubble);

        messages.appendChild(wrapper);

        messages.scrollTop =
            messages.scrollHeight;
    }


    /* =====================================================
       FAQ RESPONSE
       ===================================================== */

    function getBotResponse(question) {

        const q =
            question
                .toLowerCase()
                .trim();


        if (
            q.includes("hello") ||
            q.includes("hi") ||
            q.includes("hey")
        ) {
            return "Hello! Welcome to Literary Emporium. How can I help you?";
        }


        if (
            q.includes("book") &&
            (
                q.includes("search") ||
                q.includes("find")
            )
        ) {
            return "You can search books from the search bar on the Home page using the book title or author name.";
        }


        if (
            q.includes("category") ||
            q.includes("categories")
        ) {
            return "Literary Emporium includes Fiction, Non-Fiction, Action / Adventure, Romance, Mystery / Thriller, Science Fiction, Biography, History, Academic, Self-Help, Children and Fantasy.";
        }


        if (
            q.includes("cart") &&
            (
                q.includes("add") ||
                q.includes("how")
            )
        ) {
            return "Open a book, select Add to Cart, then open the Cart page to review your selected books.";
        }


        if (
            q.includes("wishlist")
        ) {
            return "You can add a book to your Wishlist from the Book Details page and view saved books from the Wishlist page.";
        }


        if (
            q.includes("checkout") ||
            q.includes("payment")
        ) {
            return "From the Cart page, select Proceed to Checkout, enter the required details and continue with the available payment option.";
        }


        if (
            q.includes("order") &&
            (
                q.includes("track") ||
                q.includes("status")
            )
        ) {
            return "You can check your order status from the My Orders page. The seller can update the order status from the seller order management page.";
        }


        if (
            q.includes("cancel") &&
            q.includes("order")
        ) {
            return "Orders can be cancelled while they are in the allowed early order stages. Once an order is shipped or delivered, cancellation is not available.";
        }


        if (
            q.includes("seller")
        ) {
            return "Sellers can register, log in, add books, manage their books and view incoming customer orders.";
        }


        if (
            q.includes("admin")
        ) {
            return "The admin dashboard allows management of buyers, sellers, books and orders.";
        }


        if (
            q.includes("delivery")
        ) {
            return "Delivery information is shown during the checkout and order process.";
        }


        if (
            q.includes("price") ||
            q.includes("cost")
        ) {
            return "Book prices are displayed on the book cards and Book Details page before checkout.";
        }


        if (
            q.includes("thank")
        ) {
            return "You're welcome! I'm happy to help.";
        }


        return "I can help with books, categories, search, cart, wishlist, checkout, orders, sellers and admin features. Please ask about one of these.";
    }


    /* =====================================================
       SEND MESSAGE
       ===================================================== */

    function sendMessage() {

        const input =
            document.getElementById("leChatInput");

        if (!input) {
            return;
        }

        const text =
            input.value.trim();

        if (!text) {
            return;
        }

        addMessage(text, "user");

        input.value = "";

        const answer =
            getBotResponse(text);

        setTimeout(function () {

            addMessage(answer, "bot");

            speakText(answer);

        }, 250);
    }


    /* =====================================================
       QUICK QUESTIONS
       ===================================================== */

    function leBuildQuickQuestions() {

    const container = document.getElementById("leQuickQuestions");

    if (!container) {
        return;
    }

    const questions = [
        "How do I search books?",
        "How do I add a book to cart?",
        "How do I checkout?",
        "How do I track my order?",
        "How does seller work?"
    ];

    container.innerHTML = "";

    questions.forEach(function(question) {

        const button = document.createElement("button");

        button.type = "button";
        button.className = "le-question";
        button.textContent = question;

        button.addEventListener("click", function() {

            const input =
                document.getElementById("leChatInput");

            if (!input) {
                return;
            }

            input.value = question;
            sendMessage();

        });

        container.appendChild(button);

    });
}

    /* =====================================================
       INITIALISE
       ===================================================== */

    function initChatbot() {

        createChatbot();

        const openButton =
            document.getElementById(
                "leChatOpenButton"
            );

        const closeButton =
            document.getElementById(
                "leChatClose"
            );

        const panel =
            document.getElementById(
                "leChatPanel"
            );

        const sendButton =
            document.getElementById(
                "leSendButton"
            );

        const input =
            document.getElementById(
                "leChatInput"
            );


        if (openButton) {

            openButton.addEventListener(
                "click",
                function () {

                    panel.style.display =
                        "block";

                    openButton.style.display =
                        "none";

                    input.focus();

                }
            );

        }


        if (closeButton) {

            closeButton.addEventListener(
                "click",
                function () {

                    panel.style.display =
                        "none";

                    openButton.style.display =
                        "block";

                }
            );

        }


        if (sendButton) {

            sendButton.addEventListener(
                "click",
                sendMessage
            );

        }


        if (input) {

            input.addEventListener(
                "keydown",
                function (event) {

                    if (
                        event.key ===
                        "Enter"
                    ) {
                        sendMessage();
                    }

                }
            );

        }


        leBuildQuickQuestions();

        leSetupRecognition();

        addMessage(
            "Hello! Welcome to Literary Emporium. Ask me about books, cart, checkout, orders or seller features.",
            "bot"
        );
    }


    /* =====================================================
       START
       ===================================================== */

    if (
        document.readyState ===
        "loading"
    ) {

        document.addEventListener(
            "DOMContentLoaded",
            initChatbot
        );

    } else {

        initChatbot();

    }

})();