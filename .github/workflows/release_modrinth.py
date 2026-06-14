import json
import os
import requests
from contextlib import ExitStack
from pathlib import Path


def main():
    MODRINTH_TOKEN = os.environ.get('MODRINTH_TOKEN')

    metadata = {}

    metadata["name"] = os.environ.get('VERSION')
    metadata["version_number"] = os.environ.get('VERSION').removeprefix('v')
    metadata["changelog"] = Path('CHANGELOG.md').read_text(encoding='utf-8')
    metadata['dependencies'] = []
    metadata['game_versions'] = ['1.21.1']
    metadata['version_type'] = 'release'
    metadata['loaders'] = ['forge']
    metadata['project_id'] = os.environ.get('MODRINTH_PROJECT_ID')

    with ExitStack() as stack:
        files = {}
        file_parts = []

        for jar in Path().glob('*.jar'):
            file = stack.enter_context(jar.open('rb'))
            file_part = jar.stem
            file_parts.append(file_part)
            files[file_part] = (jar.name, file, "application/java-archive")

        metadata["file_parts"] = file_parts
        metadata["primary_file"] = file_parts[0]

        response = requests.post(
            'https://api.modrinth.com/v2/version',
            headers={
                'Authorization': MODRINTH_TOKEN,
                'User-Agent': f'{os.environ.get('REPOSITORY')}/{os.environ.get('VERSION')}'
            },
            data={
                'data': json.dumps(metadata)
            },
            files=files
        )
        response.raise_for_status()

if __name__ == '__main__':
    main()
