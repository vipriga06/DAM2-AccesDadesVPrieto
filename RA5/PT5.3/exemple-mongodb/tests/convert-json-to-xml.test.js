const { buildXmlObjectFromJson } = require('../src/convert-json-to-xml');

describe('buildXmlObjectFromJson', () => {
  it('converteix un JSON bàsic al format XML esperat', () => {
    const input = {
      youtubers: [
        {
          id: '1',
          channel: 'Canal Prova',
          name: 'Persona Prova',
          subscribers: 100,
          joinDate: '2024-01-01',
          categories: ['Education'],
          videos: [
            {
              id: 'v1',
              title: 'Video Prova',
              duration: '01:00',
              views: 10,
              uploadDate: '2024-01-02',
              likes: 2,
              comments: 1
            }
          ]
        }
      ]
    };

    const xmlObject = buildXmlObjectFromJson(input);

    expect(xmlObject).toMatchObject({
      youtubers: {
        youtuber: [
          {
            $: { id: '1' },
            channel: 'Canal Prova',
            name: 'Persona Prova',
            categories: { category: ['Education'] },
            videos: {
              video: [
                {
                  $: { id: 'v1' },
                  title: 'Video Prova'
                }
              ]
            }
          }
        ]
      }
    });
  });

  it('llança error si falta la clau youtubers', () => {
    expect(() => buildXmlObjectFromJson({})).toThrow(/youtubers/);
  });
});
