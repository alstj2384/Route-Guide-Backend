document.getElementById('getMemberInfoBtn').addEventListener('click', async () => {
    try {
        const response = await fetch('http://localhost:8080/member/user1', {
            method: 'GET',
            credentials: 'include' // include cookies in the request
        });

        if (response.ok) {
            const data = await response.json();
            alert(`User Info: ${JSON.stringify(data)}`);
        } else if (response.status === 401) {
            alert("로그인을 하세요!");
        } else {
            alert(`Error: ${response.statusText}`);
        }
    } catch (error) {
        alert(`Error: ${error.message}`);
    }
});
