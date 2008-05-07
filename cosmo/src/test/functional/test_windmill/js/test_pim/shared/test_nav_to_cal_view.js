
pimTest.shared.test_navToCalView = [
  { method: "waits.sleep", params: { milliseconds : 2000 } },
  { method: "click", params: {  id: "cosmoViewToggleCalViewSelector" } },
  { method: "waits.forElement", params: { id: "calViewNav" } }
];

