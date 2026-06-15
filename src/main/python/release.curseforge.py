import json
import os
from contextlib import ExitStack
from pathlib import Path

import requests

def main():
    metadata = {
        "changelog": Path("CHANGELOG.md").read_text(encoding="utf-8").replace("\n", "<br> ").replace("\r", "<br> "),
        "changelogType": "markdown",
        "displayName": f"{os.environ.get("REPOSITORY_NAME")} {os.environ.get("VERSION")}",
        "gameVersions": [ 11779 ],  # Minecraft 1.21.1
        "gameVersionNames": ["Client", "Server", "NeoForge", "1.21.1"],
        "releaseType": "release",
        "relations": {
            "projects": []
        }
    }

    with open("src/main/python/dependencies.curseforge.json", "r", encoding="utf-8") as dependencies_file:
        metadata["relations"]["projects"] = json.load(dependencies_file)

    for jar in Path().glob("*.jar"):
        with jar.open("rb") as jar_file:
            response = requests.post(
                f"https://minecraft.curseforge.com/api/projects/{os.environ.get("CURSEFORGE_PROJECT_ID")}/upload-file",
                headers={
                    "X-Api-Token": os.environ.get("CURSEFORGE_TOKEN"),
                    "User-Agent": f"{os.environ.get("REPOSITORY")}/{os.environ.get("VERSION")}"
                },
                data={
                    "metadata": json.dumps(metadata)
                },
                files={
                    "file": (
                        jar.name,
                        jar_file,
                        "application/java-archive"
                    )
                }
            )
            print(json.dumps(metadata, indent=2, ensure_ascii=False))
            print(response.text)
            response.raise_for_status()


if __name__ == "__main__":
    main()
