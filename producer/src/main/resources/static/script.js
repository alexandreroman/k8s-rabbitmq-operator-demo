function onIncrementCounter() {
    console.log("Increment counter");
    axios.post("/counter/increment", {})
        .catch(function (error) {
            console.log("Failed to increment counter: " + error);
        });
}
