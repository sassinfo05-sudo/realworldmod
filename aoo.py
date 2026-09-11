from openai import OpenAI

client = OpenAI()

stream = client.responses.create(
    model="openai.gpt-oss-120b",
    input=[
        {"role": "user", "content": "Tell me a short story about a robot."}
    ],
    stream=True
)

for event in stream:
    print(event)